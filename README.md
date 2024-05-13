User Registration and Login System Documentation
1. User Registration
1.1 Entity:
id: Auto-generated unique identifier
email id: User's email address
Mobile NO: User's mobile number
User Name: User's name
1.2 Registration Process:
Backend Validation:

Email Id validation: Ensures the email format is valid.
Mobile No validation: Validates that the Mobile No starts with "6, 7, 8, or 9".
Registration Flow:

User provides necessary details (email id, Mobile NO, User Name).
Backend validation is performed.
If validation passes, the user is registered, and a success response is returned: "User successfully registered."
Error Handling:

If Mobile No or Email Id is not valid, an appropriate error message is returned.
2. User Login
2.1 Login Process:
User Authentication:

User logs in using their registered email id.
Email Availability Check:

Checks if the provided email id exists in the database.
OTP Generation:

If the email id exists, a 6-digit OTP is generated and sent to the user's email id.
2.2 OTP Validation:
OTP Format:

OTP should be 6 digits.
OTP Expiry:

The OTP is valid for 2 minutes.
Incorrect Attempts:

If the user enters the wrong OTP more than 3 times, the user's account is locked for 5 minutes.
This documentation provides an overview of the User Registration and Login System, including entity details, registration process, and login process with OTP validation.
