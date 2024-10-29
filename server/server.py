import grpc
from concurrent import futures
import pet_adoption_pb2
import pet_adoption_pb2_grpc
import threading

# Define the service implementation
class PetAdoptionService(pet_adoption_pb2_grpc.PetAdoptionServicer):
    def __init__(self):
        self.pets = []
        self.lock = threading.Lock()

    def RegisterPet(self, request, context):
        # Register a new pet, store the information in a list
        with self.lock:
            self.pets.append(request)
            return pet_adoption_pb2.PetResponse(message=f"Pet {request.name} registered successfully")

    def SearchPet(self, request, context):
        # Search for pets based on the provided keyword (name, gender, age, or breed)
        with self.lock:
            results = [pet for pet in self.pets if request.keyword.lower() in
                       (pet.name.lower(), pet.gender.lower(), str(pet.age), pet.breed.lower())]
            return pet_adoption_pb2.SearchResponse(pets=results)

# Function to start the server
def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    pet_adoption_pb2_grpc.add_PetAdoptionServicer_to_server(PetAdoptionService(), server)
    server.add_insecure_port('[::]:50051')
    print("Server is running on port 50051...")
    server.start()
    server.wait_for_termination()

if __name__ == '__main__':
    serve()
