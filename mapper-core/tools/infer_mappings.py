#!/usr/bin/env python3
"""
Infer mappings for not-started rows using multi-segment suffix + type similarity heuristics.
Writes mapper-core/docs/mapping_matrix_inferred.csv and prints a short summary.
"""
import csv
from collections import defaultdict
import os

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(__file__)))
IN_CSV = os.path.join(ROOT, 'mapper-core', 'docs', 'mapping_matrix_exhaustive.csv')
OUT_CSV = os.path.join(ROOT, 'mapper-core', 'docs', 'mapping_matrix_inferred.csv')


def normalize_path(path):
    # normalize dotted paths: remove empty, trim, and remove [] markers from segments for matching
    if not path:
        return []
    segs = [s.strip() for s in path.split('.') if s.strip()]
    # keep '[]' in a parallel flag
    norm = []
    for s in segs:
        norm.append(s.replace('[]',''))
    return norm


def is_list_path(path):
    return '[]' in (path or '')


def simple_type(t):
    if not t:
        return ''
    t = t.strip()
    # remove generics: List<...> -> ... ; Map<X,Y> -> X,Y (not used)
    if '<' in t and '>' in t:
        inner = t[t.find('<')+1:t.rfind('>')]
        # take the last type token
        parts = [p.strip() for p in inner.replace('>',',').split(',') if p.strip()]
        if parts:
            return parts[-1].split('.')[-1]
    # take last dot-separated
    return t.split('.')[-1]


# read rows
rows = []
with open(IN_CSV, newline='') as f:
    reader = csv.DictReader(f)
    header = reader.fieldnames
    for r in reader:
        rows.append(r)

# build suffix -> set of (targetPath, targetType, row_index)
suffix_map = defaultdict(list)
for i, r in enumerate(rows):
    tp = (r.get('targetPath') or '').strip()
    if not tp:
        continue
    segs = normalize_path(tp)
    # limit suffix length to 6 to keep map size reasonable
    max_k = min(len(segs), 6)
    for k in range(1, max_k+1):
        key = tuple(segs[-k:])
        suffix_map[(k, key)].append(i)

# helper to test type similarity
def type_similar(src_t, tgt_t):
    s = simple_type(src_t)
    t = simple_type(tgt_t)
    if not s or not t:
        # if one is empty, be conservative and require the other to be non-empty
        return False
    if s == t:
        return True
    # allow minor mismatches like PartyIdentification8 -> BranchAndFinancialInstitutionIdentification3 ? no
    # we restrict to exact simple-name equality
    return False

inferred = []
ambiguous = 0
skipped = 0

# process rows
for i, r in enumerate(rows):
    src = (r.get('sourcePath') or '').strip()
    if not src:
        skipped += 1
        continue
    if (r.get('targetPath') or '').strip():
        continue
    src_segs = normalize_path(src)
    if not src_segs:
        skipped += 1
        continue
    src_is_list = is_list_path(r.get('sourcePath'))
    src_type = (r.get('sourceType') or '').strip()

    found = False
    # try longer suffixes first
    for k in range(min(len(src_segs), 6), 0, -1):
        key = tuple(src_segs[-k:])
        candidates_idx = suffix_map.get((k, key), [])
        if not candidates_idx:
            continue
        # filter by list-compatibility and type similarity
        cands = []
        for idx in candidates_idx:
            tr = rows[idx]
            tgt_path = (tr.get('targetPath') or '').strip()
            tgt_type = (tr.get('targetType') or '').strip()
            tgt_is_list = is_list_path(tgt_path)
            # prefer same list-ness
            if src_is_list != tgt_is_list:
                continue
            # prefer type similarity
            if src_type and tgt_type:
                if not type_similar(src_type, tgt_type):
                    continue
            # accept candidate
            cands.append((idx, tgt_path, tgt_type))
        # if exactly one candidate -> infer
        if len(cands) == 1:
            idx, tgt_path, tgt_type = cands[0]
            example_src = rows[idx].get('sourcePath','')
            r['targetPath'] = tgt_path
            r['targetType'] = tgt_type
            r['mappingStrategy'] = 'heuristic-multisuffix-type'
            r['status'] = 'done'
            note = r.get('notes','') or ''
            add = f'inferred-from-target:{tgt_path}'
            if note:
                r['notes'] = note + ';' + add
            else:
                r['notes'] = add
            inferred.append((r.get('sourcePath'), tgt_path))
            found = True
            break
        elif len(cands) > 1:
            ambiguous += 1
            found = False
            break
    if not found:
        # no confident inference
        continue

# write out
with open(OUT_CSV, 'w', newline='') as f:
    writer = csv.DictWriter(f, fieldnames=header)
    writer.writeheader()
    writer.writerows(rows)

print('input=', IN_CSV)
print('output=', OUT_CSV)
print('total_rows=', len(rows))
print('inferred_count=', len(inferred))
print('ambiguous_skipped=', ambiguous)
print('skipped_empty_source_or_target=', skipped)
print('sample_inferred:')
for s,t in inferred[:50]:
    print(s, '=>', t)

# exit code 0
