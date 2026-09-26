# Dreamz Shop - A-Z Fix Pass (2026-09-26)

This package was rebuilt from the uploaded project ZIP and audited across controllers, services, repositories, entities, Thymeleaf templates, security configuration, static assets, and admin navigation.

## Critical fixes

- Fixed MySQL `Public Key Retrieval is not allowed` JDBC configuration.
- Added `/error` and common static resource access to Spring Security.
- Stabilized eager CSRF token initialization for Thymeleaf/Spring Security first-load rendering.
- Added CSRF hidden fields to POST forms that were missing them.
- Fixed customer return form model: the form now receives both `order` and `returnRequest`.
- Added compatibility route for legacy `/customer/orders/{orderId}/return/{returnId}` links; it redirects to `/customer/returns/{returnId}`.
- Fixed admin order status dropdown so the current status is always displayed and valid next statuses are offered.
- Fixed admin return status dropdown using the current status plus valid next transitions.
- Fixed order-cancellation stock restoration so product stock is actually increased and the inventory transaction uses the correct `newStock` value.
- Fixed order-item creation to use the cart item's price snapshot instead of silently replacing it with the current product selling price.
- Preserved category/subcategory relationship validation.
- Preserved safe product soft-delete behavior when historical references exist.
- Preserved return inventory restoration at the `RECEIVED` stage.

## Admin panel

- All admin templates include the common admin layout fragment.
- All admin templates load the common admin CSS and JS.
- The common sidebar/topbar is used consistently; legacy page-specific sidebars are hidden/normalized.
- Active navigation is determined from the current `/admin/...` route.
- Responsive admin sidebar/mobile drawer is retained.
- Removed stale `/admin/users` links that had no controller route.
- Categories, subcategories, brands, products, inventory, orders, returns, refunds, coupons, offers, banners, reviews, customers, reports, and staff routes remain wired to their controllers.

## Return flow

Canonical customer return URLs:

- `/customer/returns`
- `/customer/returns/request/{orderId}`
- `/customer/returns/{returnId}`
- `/customer/returns/{returnId}/cancel`

Compatibility URL:

- `/customer/orders/{orderId}/return/{returnId}` -> redirects to canonical return details.

Return status lifecycle remains:

`REQUESTED -> APPROVED -> PICKUP_SCHEDULED -> PICKED_UP -> RECEIVED -> REFUND_INITIATED -> COMPLETED`

Rejected/cancelled requests remain terminal.

## Notes

The source was statically audited and patched. A full Maven compile could not be executed in this environment because Maven 3.9.16 is not cached and external Maven Central downloads are unavailable. The package therefore does not claim a runtime build/test result that was not actually executed here.
