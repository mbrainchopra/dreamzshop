# Profile page fix

Fixed the customer profile page rendering issue:

- Corrected the password form model attribute to use `passwordRequest`, matching `CustomerProfileController`.
- Removed the stray duplicate `</a>` in `fragments/customer-navbar.html`.
- Refreshed target/classes copies of the affected templates.

If IntelliJ still shows the old page, stop the app, run Build > Rebuild Project, and restart.
