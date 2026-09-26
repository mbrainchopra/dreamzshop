# Return Item Fix

Fixed the customer return flow to match the existing `return_requests.order_item_id` database column.

Changes:
- Added `orderItem` mapping to `ReturnRequest`.
- Customer return URL now carries both `orderId` and `orderItemId`.
- Return form submits `orderItemId`.
- `ReturnRequestService` validates that the selected item belongs to the selected order.
- Duplicate return validation is now per order item.
- Return request save populates `order_item_id`.
- Inventory restoration on `RECEIVED` restores only the returned order item instead of every item in the order.
- Removed stale `target/` build output from the package.

Build note: Maven dependency download was unavailable in this environment, so the source was checked directly and the ZIP was packaged without stale build output.
