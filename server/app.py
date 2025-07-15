import firebase_admin
from dotenv import load_dotenv
from firebase_admin import credentials, auth
from flask import Flask, request, jsonify

# Load environment variables
load_dotenv()

app = Flask(__name__)

# Initialize Firebase Admin SDK
try:
    cred = credentials.Certificate('private-service-account-key.json')
    firebase_admin.initialize_app(cred)
except Exception as e:
    print(f"Error initializing Firebase: {e}")
    print("Please download your service account key from Firebase Console")
    print("and update the path in the code or use environment variables")

@app.route('/health', methods=['GET'])
def health_check():
    """Health check endpoint"""
    return jsonify({"status": "healthy", "service": "StudyIO Auth Server"})

@app.route('/verify-token', methods=['POST'])
def verify_token():
    """
    Endpoint to verify Firebase ID token
    Expects JSON body with 'token' field
    """
    try:
        # Get token from request
        data = request.get_json()
        if not data or 'token' not in data:
            return jsonify({
                "authenticated": False,
                "error": "No token provided"
            }), 400
        
        id_token = data['token']
        
        # Verify the token
        decoded_token = auth.verify_id_token(id_token)
        uid = decoded_token['uid']
        email = decoded_token.get('email', 'No email')
        
        return jsonify({
            "authenticated": True,
            "uid": uid,
            "email": email,
            "message": "Token is valid"
        }), 200

    except auth.ExpiredIdTokenError:
        return jsonify({
            "authenticated": False,
            "error": "Token expired"
        }), 401
    except auth.InvalidIdTokenError:
        return jsonify({
            "authenticated": False,
            "error": "Invalid token"
        }), 401
    except Exception as e:
        return jsonify({
            "authenticated": False,
            "error": f"Authentication error: {str(e)}"
        }), 500

@app.route('/protected', methods=['GET'])
def protected_route():
    """
    Protected endpoint that requires authentication
    Expects Authorization header with Bearer token
    """
    try:
        # Get token from Authorization header
        auth_header = request.headers.get('Authorization')
        if not auth_header or not auth_header.startswith('Bearer '):
            return jsonify({
                "authenticated": False,
                "error": "No authorization header or invalid format"
            }), 401
        
        id_token = auth_header.split('Bearer ')[1]
        
        # Verify the token
        decoded_token = auth.verify_id_token(id_token)
        uid = decoded_token['uid']
        email = decoded_token.get('email', 'No email')
        
        return jsonify({
            "authenticated": True,
            "message": f"Hello {email}! You are authenticated.",
            "uid": uid
        }), 200

    except auth.ExpiredIdTokenError:
        return jsonify({
            "authenticated": False,
            "message": "not authenticated - token expired"
        }), 401
    except auth.InvalidIdTokenError:
        return jsonify({
            "authenticated": False,
            "message": "not authenticated - invalid token"
        }), 401
    except Exception as e:
        return jsonify({
            "authenticated": False,
            "message": f"not authenticated - {str(e)}"
        }), 401

if __name__ == '__main__':
    print("Starting StudyIO Auth Server...")
    print("Server will be accessible at:")
    print("  - Local: http://127.0.0.1:5000")
    print("  - IMPORTANT: ensure that port forwarding is set up correctly for wherever yoru machine is running so that it can access this server")
    print("  - adb reverse tcp:5000 tcp:5000 (if running on Android emulator or physical device)")
    app.run(debug=True, host='0.0.0.0', port=5000)
