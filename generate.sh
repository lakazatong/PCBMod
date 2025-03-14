#!/bin/bash

cd src/main/resources/assets/pcbmod/blockstates/ || exit
python3 generate_hub_blockstates.py
cd - || exit
cd src/main/resources/assets/pcbmod/models/block/ || exit
python3 generate_hub_block_models.py
cd - || exit
