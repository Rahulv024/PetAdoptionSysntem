package com.example.ds;

import io.grpc.ManagedChannel; //Used to create channel for communication
import io.grpc.ManagedChannelBuilder;
import petadoption.PetAdoptionGrpc;
import petadoption.PetAdoptionOuterClass.*;

import java.io.IOException;
import java.nio.file.Files;//Used to read the pet picture
import java.nio.file.Paths;

public class client {

    private final PetAdoptionGrpc.PetAdoptionBlockingStub blockingStub;//blocking stub is an instance of PetAdoptionBlockingStub

    public client(ManagedChannel channel) {
        blockingStub = PetAdoptionGrpc.newBlockingStub(channel);//allows client to make blocking calls to the server
    }

    public void registerPet(String name, String gender, int age, String breed, String picturePath) {
        byte[] picture = new byte[0];
        try {
            picture = Files.readAllBytes(Paths.get(picturePath));
        } catch (IOException e) {
            System.out.println("Error reading the picture file: " + e.getMessage());
        }

        PetRequest request = PetRequest.newBuilder()//It creates a PetRequest object using the builder pattern.
                .setName(name)
                .setGender(gender)
                .setAge(age)
                .setBreed(breed)
                .setPicture(com.google.protobuf.ByteString.copyFrom(picture))//converts the byte array to a protobuf-compatible ByteString.
                .build();
        PetResponse response = blockingStub.registerPet(request);//The blockingStub.registerPet(request) sends the request to the gRPC server and waits for a response (PetResponse).
        System.out.println(response.getMessage());
    }

    public void searchPet(String keyword) {
        SearchRequest request = SearchRequest.newBuilder()
                .setKeyword(keyword)
                .build();
        SearchResponse response = blockingStub.searchPet(request);//It sends the request to the server using blockingStub.searchPet(request) and receives a SearchResponse.

        // Check if any pets match the search criteria
        if (response.getPetsList().isEmpty()) {
            System.out.println("No pets found for the keyword: " + keyword);
            System.out.println("-----------");
        } else {
            response.getPetsList().forEach(pet -> {
                System.out.println("Pet Found:");
                System.out.println("Name: " + pet.getName());
                System.out.println("Gender: " + pet.getGender());
                System.out.println("Age: " + pet.getAge());
                System.out.println("Breed: " + pet.getBreed());
                if (pet.getPicture().size() > 0) {
                    System.out.println("Picture data is available for this pet.");
                } else {
                    System.out.println("No picture available for this pet.");
                }
                System.out.println("-----------");
            });
        }
    }

    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("server", 50051)//It creates a gRPC channel connecting to the server at server:50051 using plaintext
                .usePlaintext()
                .build();

        client clientApp = new client(channel);//creates a new instance of the client class, named clientApp, and initializes it with a gRPC channel.

        // Update the image path to match the path inside the Docker container
        String imagePath = "/app/data/Golden-Retriever.jpeg";
        clientApp.registerPet("Buddy", "Male", 3, "Golden Retriever", imagePath);

        // Test with different search keywords
        clientApp.searchPet("Male"); // Should find the pet by name
        clientApp.searchPet("Budd"); // Should show "No pets found for the keyword: Budd"

        String imgPath = "/app/data/lab.jpeg";
        clientApp.registerPet("Max", "Female", 10, "Labrador", imgPath);

        // Test with different search keywords
        clientApp.searchPet("Labrador"); // Should find the pet by name
        clientApp.searchPet("Maxi"); // Should show "No pets found for the keyword: Maxi"

        // Shutdown the channel once done
        channel.shutdown();
    }
}
