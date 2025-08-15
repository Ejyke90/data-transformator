#!/usr/bin/env python3
# Small script derived from an ad-hoc shell-embedded Python snippet used earlier.
# Prints 20 candidate @Mapping lines for not-started cdtTrfTxInf fields, excluding existing targets.
import csv,re
from pathlib import Path
m=Path('mapper-core/src/main/java/org/translator/mapper/Pacs008ToPacs009Mapper.java').read_text()
existing=set(re.findall(r'@Mapping\(\s*target\s*=\s*"([^\"]+)"',m))
rows=[]
with open('mapper-core/docs/mapping_matrix_exhaustive.csv') as f:
    r=csv.DictReader(f)
    for row in r:
        rows.append(row)
cands=[]
for row in rows:
    if row['status'].strip()!='not-started': continue
    sp=row['sourcePath'].strip()
    if not sp.startswith('pacs00800101.cdtTrfTxInf'): continue
    rel = sp.split('pacs00800101.',1)[1]
    if rel.startswith('cdtTrfTxInf[].'):
        rel2 = rel[len('cdtTrfTxInf[].'):]
    elif rel.startswith('cdtTrfTxInf.'):
        rel2 = rel[len('cdtTrfTxInf.'):]
    else:
        rel2 = rel
    if rel2 in existing: continue
    # prefer shallower
    if rel2.count('.')>3: continue
    cands.append((rel2,row['sourceType']))
# manually prioritize common fields
priority=['instdAmt','chrgBr','chrgsInf','chrgsInf[].chrgsAmt','chrgsInf[].chrgsPty','dbtr.ctryOfRes','dbtr.id.orgId','initgPty','initgPty.ctryOfRes','initgPty.id.orgId','pmtTpInf.ctgyPurp','pmtTpInf.svcLvl.cd.SEPA','pmtTpInf.svcLvl.cd.SDVA','poolgAdjstmntDt','purp','rgltryRptg','rltdRmtInf','rmtInf.strd[].addtlRmtInf','rmtInf.strd[].cdtrRefInf','rmtInf.strd[].invcee']
out=[]
for p in priority:
    if p in existing: continue
    out.append(p)
# pad with other candidates
for r,t in cands:
    if r in out: continue
    out.append(r)
for rel in out[:20]:
    print('@Mapping(target = "%s", source = "%s")' % (rel, rel))
