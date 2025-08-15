This directory contains Python utilities and historical versions used during mapping matrix generation and inference experiments.

Files:
- infer_mappings_v1.py  : Copy of the multi-segment suffix + type similarity inference script used earlier.
- generate_candidate_mappings.py : Ad-hoc snippet that prints 20 candidate @Mapping annotations for not-started transaction fields.

Usage:
- Run the inference script (writes mapper-core/docs/mapping_matrix_inferred.csv):

```bash
python3 python/infer_mappings_v1.py
```

- Generate candidate mapping lines for quick insertion into the MapStruct mapper:

```bash
python3 python/generate_candidate_mappings.py
```
