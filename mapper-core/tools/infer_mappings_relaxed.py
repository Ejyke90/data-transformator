#!/usr/bin/env python3
"""
Relaxed mapping candidate generator: produces ranked candidate mappings using suffix-match scoring
and lightweight type heuristics. Writes mapper-core/docs/mapping_matrix_candidates_relaxed.csv
and prints a short summary and top candidates.
"""
import csv
from collections import defaultdict
import os

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(__file__)))
IN_CSV = os.path.join(ROOT, 'mapper-core', 'docs', 'mapping_matrix_exhaustive.csv')
OUT_CSV = os.path.join(ROOT, 'mapper-core', 'docs', 'mapping_matrix_candidates_relaxed.csv')


def normalize_path(path):
    if not path:
        return []
    segs = [s.strip() for s in path.split('.') if s.strip()]
    return [s.replace('[]','') for s in segs]


def is_list_path(path):
    return '[]' in (path or '')


def simple_type(t):
    if not t:
        return ''
    t = t.strip()
    if '<' in t and '>' in t:
        inner = t[t.find('<')+1:t.rfind('>')]
        parts = [p.strip() for p in inner.replace('>',',').split(',') if p.strip()]
        if parts:
            return parts[-1].split('.')[-1]
    return t.split('.')[-1]


def likely_address_type(t):
    if not t: return False
    s = simple_type(t).lower()
    return 'addr' in s or 'postal' in s or 'adr' in s


def likely_amount_type(t):
    if not t: return False
    s = simple_type(t).lower()
    return 'amt' in s or 'amount' in s or 'currency' in s or 'decimal' in s


# read rows
rows = []
with open(IN_CSV, newline='') as f:
    reader = csv.DictReader(f)
    header = reader.fieldnames
    for r in reader:
        rows.append(r)

# build suffix -> set of (target_index)
suffix_map = defaultdict(list)
for i, r in enumerate(rows):
    tp = (r.get('targetPath') or '').strip()
    if not tp:
        continue
    segs = normalize_path(tp)
    max_k = min(len(segs), 6)
    for k in range(1, max_k+1):
        key = tuple(segs[-k:])
        suffix_map[(k, key)].append(i)

candidates = []

for i, r in enumerate(rows):
    src = (r.get('sourcePath') or '').strip()
    if not src:
        continue
    if (r.get('targetPath') or '').strip():
        continue
    src_segs = normalize_path(src)
    if not src_segs:
        continue
    src_is_list = is_list_path(r.get('sourcePath'))
    src_type = (r.get('sourceType') or '').strip()

    seen_tgts = set()
    # try suffix lengths and collect scored candidates
    for k in range(min(len(src_segs), 6), 0, -1):
        key = tuple(src_segs[-k:])
        idxs = suffix_map.get((k, key), [])
        if not idxs:
            continue
        for idx in idxs:
            tr = rows[idx]
            tgt_path = (tr.get('targetPath') or '').strip()
            if not tgt_path: continue
            if tgt_path in seen_tgts: continue
            seen_tgts.add(tgt_path)
            tgt_type = (tr.get('targetType') or '').strip()
            tgt_is_list = is_list_path(tgt_path)

            # base score: suffix length weight
            score = k * 10
            # prefer same list-ness
            if src_is_list == tgt_is_list:
                score += 5
            else:
                score -= 3
            # type similarity bonus
            if simple_type(src_type) and simple_type(tgt_type) and simple_type(src_type) == simple_type(tgt_type):
                score += 8
            # penalize likely amount->address matches
            if likely_amount_type(src_type) and likely_address_type(tgt_type):
                score -= 20
            if likely_address_type(src_type) and likely_amount_type(tgt_type):
                score -= 20
            # small bonus for having explicit identifiers in path (id, Id, tax, bic)
            path_lower = tgt_path.lower()
            if any(x in path_lower for x in ['id', 'tax', 'bic', 'be', 'duns', 'ean']):
                score += 3
            # clamp
            if score < 0: score = 0
            # convert to 0..100 confidence roughly
            confidence = int(min(100, score * 4))
            candidates.append({
                'sourcePath': src,
                'sourceType': src_type,
                'targetPath': tgt_path,
                'targetType': tgt_type,
                'suffixLen': k,
                'score': score,
                'confidence': confidence,
                'mappedAlready': 'true' if (r.get('status') or '').lower() == 'done' else 'false'
            })

    # fallback: if no suffix-based candidates were found for this source, allow a conservative
    # id-based fallback that maps various source id/orgId/prvtId fields to likely branch/finInst targets
    # This helps recover reasonable candidates when segments differ (e.g., orgId -> brnchId.id)
    if not seen_tgts:
        src_last = src_segs[-1].lower()
        # only consider id-like source fields for fallback
        if any(tok in src_last for tok in ('id', 'orgid', 'prvtid', 'othrid', 'taxid', 'psptnb')):
            for j, tr in enumerate(rows):
                tgt_path = (tr.get('targetPath') or '').strip()
                if not tgt_path:
                    continue
                if tgt_path in seen_tgts:
                    continue
                tgt_segs = normalize_path(tgt_path)
                if not tgt_segs:
                    continue
                tgt_last = tgt_segs[-1].lower()
                tgt_join = '.'.join(tgt_segs).lower()
                # only propose targets that look like branch or financial institution ids to reduce false positives
                if not any(k in tgt_join for k in ('brnchid', 'brnchid.id', 'fininstn', 'fininstnid', 'cmbndid', 'prtryid', 'bic')):
                    continue
                # prefer targets that end with id
                if 'id' not in tgt_last:
                    continue
                # base low score for fallback
                score = 3
                # bonus if both source and target look id-like
                score += 4
                # small bonus for common id keywords
                if any(x in tgt_join for x in ('brnch', 'fininstn', 'bic')):
                    score += 2
                # penalize address/amount mismatches
                if likely_amount_type(src_type) and likely_address_type(tr.get('targetType') or ''):
                    score -= 10
                if score < 0: score = 0
                confidence = int(min(100, score * 6))
                candidates.append({
                    'sourcePath': src,
                    'sourceType': src_type,
                    'targetPath': tgt_path,
                    'targetType': (tr.get('targetType') or '').strip(),
                    'suffixLen': 0,
                    'score': score,
                    'confidence': confidence,
                    'mappedAlready': 'true' if (r.get('status') or '').lower() == 'done' else 'false'
                })

# sort candidates by sourcePath then confidence desc
candidates.sort(key=lambda x: (x['sourcePath'], -x['confidence'], -x['score']))

# write out candidates CSV
fields = ['sourcePath','sourceType','targetPath','targetType','suffixLen','score','confidence','mappedAlready']
with open(OUT_CSV, 'w', newline='') as f:
    writer = csv.DictWriter(f, fieldnames=fields)
    writer.writeheader()
    writer.writerows(candidates)

print('wrote candidates to', OUT_CSV)
print('total source candidate rows=', len(set(c['sourcePath'] for c in candidates)))

# print top candidates by confidence
seen = set()
count = 0
for c in candidates:
    key = (c['sourcePath'], c['targetPath'])
    if key in seen: continue
    seen.add(key)
    print(f"conf={c['confidence']:2d} score={c['score']:2d} {c['sourcePath']} -> {c['targetPath']}")
    count += 1
    if count >= 80:
        break

# also print a short top-20 per-source filtered list to help curation
print('\nTop single-candidate per source (best confidence)')
by_source = defaultdict(list)
for c in candidates:
    by_source[c['sourcePath']].append(c)

single_best = []
for s, lst in by_source.items():
    best = max(lst, key=lambda x: (x['confidence'], x['score']))
    single_best.append(best)
single_best.sort(key=lambda x: -x['confidence'])
for b in single_best[:60]:
    print(f"conf={b['confidence']:2d} score={b['score']:2d} {b['sourcePath']} -> {b['targetPath']}")

print('\ndone')
