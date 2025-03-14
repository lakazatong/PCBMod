import itertools
import json

states = {
    '0': 'close',
    '1': 'input',
    '2': 'output',
}

directions = ['north', 'east', 'south', 'west']
rotations = {'north': 0, 'east': 90, 'south': 180, 'west': 270}

blockstate_data = {"variants": {}}

for combo in itertools.product('012', repeat=6):
    state_str = ''.join(combo)
    props = (
        f"back_type={states[combo[1]]},"
        f"down_type={states[combo[5]]},"
        f"facing={{facing}},"
        f"front_type={states[combo[0]]},"
        f"left_type={states[combo[2]]},"
        f"right_type={states[combo[3]]},"
        f"up_type={states[combo[4]]}"
    )

    for facing in directions:
        key = props.format(facing=facing)
        blockstate_data["variants"][key] = {
            "model": f"pcbmod:block/hub/{state_str}",
            "y": rotations[facing]
        }

with open('hub.json', 'w') as f:
    json.dump(blockstate_data, f, indent=4)
