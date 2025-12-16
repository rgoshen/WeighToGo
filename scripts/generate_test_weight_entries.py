#!/usr/bin/env python3
"""
Generate SQL INSERT statements for bulk weight entry testing.

Usage:
    python3 generate_test_weight_entries.py > test_weight_entries.sql

    # Then apply to database:
    adb push test_weight_entries.sql /sdcard/
    adb shell run-as weightogo
    sqlite3 /data/data/weightogo/databases/WeighToGo.db
    .read /sdcard/test_weight_entries.sql
    .exit
    exit

Arguments:
    --user-id INT       User ID for weight entries (default: 1)
    --count INT         Number of entries to generate (default: 100)
    --start-weight NUM  Starting weight value (default: 170.0)
    --variance NUM      Weight fluctuation variance (default: 2.0)
    --unit STR          Weight unit: lbs, kg, or mixed (default: mixed)
"""

import argparse
import random
from datetime import datetime, timedelta


def generate_weight_entries(user_id=1, count=100, start_weight=170.0, variance=2.0, unit='mixed'):
    """
    Generate SQL INSERT statements for weight entries.

    Args:
        user_id: User ID for entries (default: 1)
        count: Number of entries to generate (default: 100)
        start_weight: Starting weight value in lbs (default: 170.0)
        variance: Maximum weight change per day (default: 2.0)
        unit: Weight unit - 'lbs', 'kg', or 'mixed' (default: 'mixed')

    Returns:
        List of SQL INSERT statements
    """
    entries = []
    current_date = datetime.now()
    current_weight = start_weight

    for i in range(count):
        entry_date = current_date - timedelta(days=i)
        date_str = entry_date.strftime('%Y-%m-%d')

        # Vary weight slightly (simulate realistic weight fluctuation)
        weight_change = random.uniform(-variance, variance)
        current_weight += weight_change

        # Clamp to reasonable range (50-500 lbs)
        current_weight = max(50.0, min(500.0, current_weight))

        # Determine unit
        if unit == 'mixed':
            # Mix of lbs and kg (70% lbs, 30% kg for realistic US distribution)
            entry_unit = "lbs" if random.random() < 0.7 else "kg"
        elif unit == 'kg':
            entry_unit = "kg"
        else:  # lbs
            entry_unit = "lbs"

        # Convert weight if kg
        display_weight = current_weight
        if entry_unit == "kg" and start_weight > 100:
            # If start weight looks like lbs, convert to kg
            display_weight = current_weight * 0.453592

        sql = f"""INSERT INTO daily_weights (user_id, weight_date, weight_value, weight_unit, created_at, updated_at, is_deleted)
VALUES ({user_id}, '{date_str}', {display_weight:.1f}, '{entry_unit}', datetime('now'), datetime('now'), 0);"""

        entries.append(sql)

    return entries


def main():
    parser = argparse.ArgumentParser(
        description='Generate SQL INSERT statements for bulk weight entry testing',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  # Generate 100 mixed-unit entries for user 1
  python3 generate_test_weight_entries.py > test_data.sql

  # Generate 200 lbs-only entries starting at 180 lbs
  python3 generate_test_weight_entries.py --count 200 --start-weight 180 --unit lbs

  # Generate 50 kg entries with high variance
  python3 generate_test_weight_entries.py --count 50 --start-weight 80 --variance 3.0 --unit kg
        """
    )

    parser.add_argument('--user-id', type=int, default=1,
                        help='User ID for weight entries (default: 1)')
    parser.add_argument('--count', type=int, default=100,
                        help='Number of entries to generate (default: 100)')
    parser.add_argument('--start-weight', type=float, default=170.0,
                        help='Starting weight value (default: 170.0)')
    parser.add_argument('--variance', type=float, default=2.0,
                        help='Weight fluctuation variance (default: 2.0)')
    parser.add_argument('--unit', choices=['lbs', 'kg', 'mixed'], default='mixed',
                        help='Weight unit: lbs, kg, or mixed (default: mixed)')

    args = parser.parse_args()

    # Generate entries
    entries = generate_weight_entries(
        user_id=args.user_id,
        count=args.count,
        start_weight=args.start_weight,
        variance=args.variance,
        unit=args.unit
    )

    # Print SQL header
    print("-- WeightToGo Test Weight Entries")
    print(f"-- Generated: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print(f"-- User ID: {args.user_id}")
    print(f"-- Count: {args.count}")
    print(f"-- Start Weight: {args.start_weight} {args.unit}")
    print(f"-- Variance: ±{args.variance}")
    print()
    print("-- Begin transaction")
    print("BEGIN TRANSACTION;")
    print()

    # Print INSERT statements
    for sql in entries:
        print(sql)

    # Print footer
    print()
    print("COMMIT;")
    print()
    print(f"-- Done. Total entries: {args.count}")
    print("-- Apply to database:")
    print("--   adb push <this-file>.sql /sdcard/")
    print("--   adb shell run-as weightogo")
    print("--   sqlite3 /data/data/weightogo/databases/WeighToGo.db")
    print("--   .read /sdcard/<this-file>.sql")
    print("--   .exit")
    print("--   exit")


if __name__ == "__main__":
    main()
