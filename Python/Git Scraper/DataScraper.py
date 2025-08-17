import os
import requests

# Base URL for raw CSVs
base_url = "https://raw.githubusercontent.com/Tennismylife/TML-Database/master"

# Local save folder
output_dir = os.path.join("..", "..", "Data", "OriginalCSVs")
os.makedirs(output_dir, exist_ok=True)

# Download all years 1968–2025
for year in range(1968, 2026):
    file = f"{year}.csv"
    file_path = os.path.join(output_dir, file)

    # Skip if file already exists
    if os.path.exists(file_path):
        print(f"Already exists, skipping: {file}")
        continue

    url = f"{base_url}/{file}"
    r = requests.get(url)
    if r.status_code == 200:
        with open(file_path, "wb") as f:
            f.write(r.content)
        print(f"Downloaded {file}")
    else:
        print(f"Failed to download {file}")
