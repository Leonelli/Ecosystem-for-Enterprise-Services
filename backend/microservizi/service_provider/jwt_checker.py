from cryptography.x509 import load_pem_x509_certificate
from cryptography.hazmat.backends import default_backend
from cryptography.x509.oid import NameOID
import jwt



JWT = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJBQjEyM0NEIiwiTWFyY2EiOiJGaWF0IiwiTW9kZWxsbyI6IjUwMCIsImlzcyI6Ik1STE1SVDg4UzIzTDg0NVJcLzAwMDAwMDAwMDAwMSIsImlhdCI6MTYyMzY3NjkxNzIzMCwiZXhwIjoxNjIzNjc3MDAzNjMwfQ.d8f9YWvAJ1YeoYhxoMP5idf7A437yazjJFnN6Zs7N81b4OHjLmxO81W3Ibtn7QE5oc47bPAL12exnSI5W7uM7KxuDfE2sg2bvTHY336OTVfeXIXe8EVXq8cUfdFelEFCuDyvcXxxPe3E2dKIJ1ZLaW80tsN-4rtPbJSjrRzePDdfhs-lXbWdUE6v15YhDfoZ5a-Xi4zuucqrI2VAI5x6b0feb6OQIWKPF3GbW1Y_7RJGmP2NC_j9i4gAhLDNuU1rxaQYXp74bKSibdVWT4pKpzuHYnL02KstIo2cIDZtY_k4faHQ3H32eidYi7cTI0rLGCrrRVTTbRkp6uG_VKPjEQ"


def jwt_reader(JWT):

    with open("/home/CIE_CERT_UMB.crt", 'rb') as cert_file:
        cert = cert_file.read()
        cert_obj = load_pem_x509_certificate(cert, default_backend())
        cn_cert = cert_obj.subject.get_attributes_for_oid(NameOID.COMMON_NAME)[0].value
        public_key = cert_obj.public_key()

    result = True
    print("Running...")

    try:
        output = jwt.decode(JWT, public_key, algorithms=['RS256'])
        print(output)
        if (cn_cert != output["iss"]):
            #print("Firma di un soggetto diverso")
            result = False
        else:
            #print("Firma dello stesso soggetto")
            #if ....
            result = True

    except jwt.ExpiredSignatureError:
        print("Token scaduto")
        result = False
    except jwt.exceptions.InvalidSignatureError:
        print("Firma non valida")
        result = False
    except Exception as e:
        print(e)
        result = None
    return  result

#print(jwt_reader(JWT))