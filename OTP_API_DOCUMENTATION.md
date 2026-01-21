# OTP Failover API Documentation

## Overview

When Silent Network Authentication (OAuth 3-legged flow) fails, the mobile app can use the OTP Failover system to authenticate users via SMS One-Time Password.

---

## Authentication Flow

```
┌─────────────────────────────────────────────────────────────┐
│                   OTP Failover Flow                          │
└─────────────────────────────────────────────────────────────┘

Step 1: OAuth 3-Legged API Fails
        Mobile App → POST /api/oauth/initiate
                 ↓
        Response: {
          "success": false,
          "error": "not_found_msisdn",
          "error_description": "Phone Number is Missing"
        }

Step 2: Show "Send OTP" Button
        User clicks "Send OTP" button in mobile app
                 ↓
        Mobile App → POST /api/otp/send
                 ↓
        SMS OTP sent to user's phone number

Step 3: User Enters OTP
        User receives SMS and enters 6-digit code
                 ↓
        Mobile App → POST /api/otp/verify
                 ↓
        ✅ User Authenticated
```

---

## API Endpoints

### Base URL

- **Development:** `http://localhost:5000`
- **Production:** `https://cyn-initial-server-impan.ondigitalocean.app`

---

## 1. Send OTP

Sends a 6-digit OTP code to the user's phone number via SMS.

### Endpoint

```
POST /api/otp/send
```

### Request Headers

```
Content-Type: application/json
```

### Request Body

```json
{
  "phoneNumber": "+639292770868"
}
```

| Field       | Type   | Required | Description                                        |
| ----------- | ------ | -------- | -------------------------------------------------- |
| phoneNumber | string | Yes      | Phone number in E.164 format (e.g., +639292770868) |

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "OTP sent successfully to your phone number",
  "phoneNumber": "+639292770868",
  "expiresIn": 180,
  "timestamp": "2026-01-16T10:30:00.000Z"
}
```

| Field       | Type    | Description                               |
| ----------- | ------- | ----------------------------------------- |
| success     | boolean | Always `true` on success                  |
| message     | string  | Success message                           |
| phoneNumber | string  | Phone number OTP was sent to              |
| expiresIn   | number  | OTP validity in seconds (180 = 3 minutes) |
| timestamp   | string  | ISO 8601 timestamp                        |

### Error Responses

#### 400 Bad Request - Missing Phone Number

```json
{
  "error": "MISSING_PHONE_NUMBER",
  "error_description": "Phone number is required",
  "success": false
}
```

#### 400 Bad Request - Invalid Phone Format

```json
{
  "error": "INVALID_PHONE_NUMBER",
  "error_description": "Phone number must be in E.164 format (e.g., +639214830648)",
  "success": false
}
```

#### 500 Internal Server Error - SMS Send Failed

```json
{
  "error": "OTP_SEND_FAILED",
  "error_description": "Failed to send OTP via SMS",
  "message": "SMS API error details",
  "success": false
}
```

### Example Request (cURL)

```bash
curl -X POST https://cyn-initial-server-impan.ondigitalocean.app/api/otp/send \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "+639292770868"
  }'
```

### Example Request (JavaScript/Fetch)

```javascript
const response = await fetch(
  "https://cyn-initial-server-impan.ondigitalocean.app/api/otp/send",
  {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      phoneNumber: "+639292770868",
    }),
  }
);

const data = await response.json();

if (data.success) {
  console.log("OTP sent! Valid for", data.expiresIn, "seconds");
  // Show OTP input field to user
} else {
  console.error("Failed to send OTP:", data.error_description);
}
```

---

## 2. Verify OTP

Verifies the 6-digit OTP code entered by the user.

### Endpoint

```
POST /api/otp/verify
```

### Request Headers

```
Content-Type: application/json
```

### Request Body

```json
{
  "phoneNumber": "+639292770868",
  "otp": "123456"
}
```

| Field       | Type   | Required | Description                  |
| ----------- | ------ | -------- | ---------------------------- |
| phoneNumber | string | Yes      | Phone number in E.164 format |
| otp         | string | Yes      | 6-digit OTP code from SMS    |

### Success Response (200 OK)

```json
{
  "success": true,
  "valid": true,
  "message": "Phone number verified successfully via OTP",
  "phoneNumber": "+639292770868",
  "verified": true,
  "timestamp": "2026-01-16T10:33:00.000Z"
}
```

| Field       | Type    | Description              |
| ----------- | ------- | ------------------------ |
| success     | boolean | Always `true` on success |
| valid       | boolean | OTP validation result    |
| message     | string  | Success message          |
| phoneNumber | string  | Verified phone number    |
| verified    | boolean | Verification status      |
| timestamp   | string  | ISO 8601 timestamp       |

### Error Responses

#### 400 Bad Request - Missing Parameters

```json
{
  "error": "MISSING_PARAMETERS",
  "error_description": "Phone number and OTP are required",
  "success": false
}
```

#### 400 Bad Request - Invalid OTP

```json
{
  "error": "OTP_VERIFICATION_FAILED",
  "error_description": "Invalid OTP code. Please try again.",
  "success": false,
  "valid": false
}
```

#### 400 Bad Request - OTP Expired

```json
{
  "error": "OTP_VERIFICATION_FAILED",
  "error_description": "OTP has expired. Please request a new one.",
  "success": false,
  "valid": false
}
```

#### 400 Bad Request - No OTP Found

```json
{
  "error": "OTP_VERIFICATION_FAILED",
  "error_description": "No OTP found or OTP has expired. Please request a new one.",
  "success": false,
  "valid": false
}
```

### Example Request (cURL)

```bash
curl -X POST https://cyn-initial-server-impan.ondigitalocean.app/api/otp/verify \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "+639292770868",
    "otp": "123456"
  }'
```

### Example Request (JavaScript/Fetch)

```javascript
const response = await fetch(
  "https://cyn-initial-server-impan.ondigitalocean.app/api/otp/verify",
  {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      phoneNumber: "+639292770868",
      otp: "123456",
    }),
  }
);

const data = await response.json();

if (data.success && data.verified) {
  console.log("✅ User authenticated successfully!");
  // Proceed with user login
} else {
  console.error("❌ OTP verification failed:", data.error_description);
  // Show error to user
}
```

---

## Complete Mobile App Integration Example

### React Native Example

```javascript
import React, { useState } from "react";
import { View, Text, Button, TextInput, Alert } from "react-native";

const OTPFailoverScreen = ({ phoneNumber }) => {
  const [otp, setOtp] = useState("");
  const [otpSent, setOtpSent] = useState(false);
  const [loading, setLoading] = useState(false);

  const BASE_URL = "https://cyn-initial-server-impan.ondigitalocean.app";

  // Step 1: Send OTP
  const handleSendOTP = async () => {
    setLoading(true);
    try {
      const response = await fetch(`${BASE_URL}/api/otp/send`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ phoneNumber }),
      });

      const data = await response.json();

      if (data.success) {
        setOtpSent(true);
        Alert.alert("Success", `OTP sent! Valid for ${data.expiresIn} seconds`);
      } else {
        Alert.alert("Error", data.error_description);
      }
    } catch (error) {
      Alert.alert("Error", "Failed to send OTP");
    } finally {
      setLoading(false);
    }
  };

  // Step 2: Verify OTP
  const handleVerifyOTP = async () => {
    setLoading(true);
    try {
      const response = await fetch(`${BASE_URL}/api/otp/verify`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ phoneNumber, otp }),
      });

      const data = await response.json();

      if (data.success && data.verified) {
        Alert.alert("Success", "User authenticated!");
        // Navigate to home screen or save auth token
      } else {
        Alert.alert("Error", data.error_description);
      }
    } catch (error) {
      Alert.alert("Error", "Failed to verify OTP");
    } finally {
      setLoading(false);
    }
  };

  return (
    <View>
      <Text>Phone: {phoneNumber}</Text>

      {!otpSent ? (
        <Button title="Send OTP" onPress={handleSendOTP} disabled={loading} />
      ) : (
        <>
          <TextInput
            placeholder="Enter 6-digit OTP"
            value={otp}
            onChangeText={setOtp}
            keyboardType="number-pad"
            maxLength={6}
          />
          <Button
            title="Verify OTP"
            onPress={handleVerifyOTP}
            disabled={loading || otp.length !== 6}
          />
          <Button
            title="Resend OTP"
            onPress={handleSendOTP}
            disabled={loading}
          />
        </>
      )}
    </View>
  );
};

export default OTPFailoverScreen;
```

---

## Phone Number Format

**All phone numbers MUST be in E.164 format:**

✅ **Correct Format:**

- `+639292770868` (Philippines)
- `+14155552671` (USA)
- `+447911123456` (UK)

❌ **Incorrect Format:**

- `639292770868` (missing +)
- `09292770868` (local format)
- `+63 929 277 0868` (contains spaces)
- `+63-929-277-0868` (contains dashes)

### Converting to E.164 Format

| Country     | Local Format   | E.164 Format  | Country Code |
| ----------- | -------------- | ------------- | ------------ |
| Philippines | 0929 277 0868  | +639292770868 | +63          |
| USA         | (415) 555-2671 | +14155552671  | +1           |
| UK          | 07911 123456   | +447911123456 | +44          |

**Format Pattern:** `+[country code][subscriber number]`

---

## Security Considerations

### OTP Expiration

- **Validity:** 3 minutes (180 seconds)
- After expiration, user must request a new OTP
- Old OTPs are automatically deleted from storage

### Rate Limiting

- API endpoints are rate-limited to prevent abuse
- Multiple failed verification attempts may trigger additional security measures

### SMS Delivery

- OTP is sent via SMS to the registered phone number
- Delivery time varies by carrier (typically 5-30 seconds)
- User should check spam/message filters if not received

---

## Testing

### Test Phone Numbers

Use your actual phone number for testing in development:

```json
{
  "phoneNumber": "+639292770868"
}
```

### Postman Collection

Import the included Postman collection for easy testing:

- Collection: `CYN_API_Testing.postman_collection.json`
- Section: **"OTP Failover (Manual)"**

---

## Error Handling Best Practices

```javascript
const sendOTP = async (phoneNumber) => {
  try {
    const response = await fetch(`${BASE_URL}/api/otp/send`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ phoneNumber }),
    });

    const data = await response.json();

    // Handle different response codes
    switch (response.status) {
      case 200:
        return { success: true, data };

      case 400:
        // Invalid phone format or missing parameter
        return {
          success: false,
          error: "Invalid phone number format",
        };

      case 500:
        // SMS API failed
        return {
          success: false,
          error: "Failed to send SMS. Please try again.",
        };

      default:
        return {
          success: false,
          error: "Unexpected error occurred",
        };
    }
  } catch (error) {
    // Network error
    return {
      success: false,
      error: "Network error. Please check your connection.",
    };
  }
};
```

---

## Support

For technical support or questions:

- GitHub: https://github.com/jonslogar2/sna-server
- Issues: https://github.com/jonslogar2/sna-server/issues

---

## Changelog

### Version 1.0.0 (January 16, 2026)

- Initial OTP Failover API release
- Send OTP endpoint
- Verify OTP endpoint
- 3-minute OTP expiration
- E.164 phone number validation
