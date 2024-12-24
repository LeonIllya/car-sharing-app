# Car Sharing Service App
___
Welcome to CarSharing — the perfect solution for modern travelers 🚗.
We designed this platform to make it easy for you to find 🚦, book 📅, and use cars for any purpose 🚙.
Whether it's a short trip around the city or a long journey, CarSharing makes renting a car simple, convenient, and accessible 🚘.
Your journey starts here — choose your car and enjoy the freedom of mobility!
___

## 🔧 Technologies and tools used
___
This project is based on a current and reliable technology 
stack that will provide high stability, scalability and security. 
A brief overview of the technologies underlying the CarSharing:

- **Java:** 17 version
- **Spring Boot:** Simplifies the development and deployment process.
- **Spring Security:** Provides comprehensive security for your application.
- **Spring Data JPA:** Manages and accesses data with ease.
- **MapStruct:** Automates the mapping of Java beans.
- **Swagger:** Enhances API documentation and testing.
- **MySQL:** Reliable relational database management system.
- **Liquibase:** Manages database schema changes.
- **Docker:** Streamlines development and deployment in isolated environments.
- **Docker Testcontainers:** Facilitates integration testing with a MySQL container.
- **Telegram Bots API:** - Integrated bot for sending notifications about rentals.
- **Stripe API:** - Integrated service for creating and processing online payments.
___
## How to Run the Project
___
### Prerequisites:
- **Java 17** or higher
- **Maven** for dependency management
- **Docker** and **Docker Compose** for setting up the environment

### Steps to Launch Application:

1. **Clone the repository:**
   ```bash
   git clone git@github.com:your-username/car-sharing-app.git
   cd car-sharing-app

2. **Set up the environment by creating a ```.env``` file with the following variables:**
   ```
   MYSQLDB_USER=<your_username>
   MYSQLDB_PASSWORD=<your_password>
   MYSQLDB_DATABASE=<your_database_name>
   MYSQLDB_LOCAL_PORT=<your_local_port>
   MYSQLDB_DOCKER_PORT=<your_docker_port>
   
   SPRING_LOCAL_PORT=<your_spring_local_port>
   SPRING_DOCKER_PORT=<your_spring_docker_port>
   DEBUG_PORT=<your_debug_port>

3. **Build and start the containers using Docker Compose:**
    ```
    docker-compose up --build
   ```
4. The application will be accessible at ```http://localhost:<YOUR_PORT>/api```.

### Running Tests:
To run unit and integration tests using Testcontainers, execute:
   ```
   mvn clean test
   ```
## 💻 Project endpoints:
___
### 🔑 Authentication Controller - endpoints for user registration and authentication of registered users. HTTP method Endpoint Description

| HTTP method |       Endpoint        |           Description           |
|:-----------:|:---------------------:|:-------------------------------:|
|    POST     | `/auth/registration ` |       Register a new user       |
|    POST     |    `/auth/login `     |   Login as a registered user    |

---
### 🚗 CarController - endpoints for managing cars.
| HTTP method |    Endpoint     |  Role   |        Description         |
|:-----------:|:---------------:|:-------:|:--------------------------:|
|    POST     |    `/cars `     | MANAGER |       Save a new car       |
|     PUT     |  `/cars/{id} `  | MANAGER |      Update car by id      |
|   DELETE    |  `/cars/{id} `  | MANAGER |      Delete car by id      |
|     GET     |  `/cars/{id}`   |   ALL   |       Get car by id        |
|     GET     |    `/cars `     |   ALL   |        Get all cars        |
|     GET     | `/cars/search ` |   ALL   | Get all cars by parameters |

---
### 🧾 PaymentController - endpoints for managing payments.
| HTTP method |       Endpoint       |  Role   |        Description         |
|:-----------:|:--------------------:|:-------:|:--------------------------:|
|    POST     |     `/payments `     |   ALL   |     Save a new payment     |
|     GET     |     `/payments`      |   ALL   |  Get all payments by user  |
|     GET     | `/payments/success ` | MANAGER |  Mark payment is success   |
|     GET     | `/payments/cancel `  | MANAGER |   Mark payment is cancel   |

---
### 🏠 RentalController - endpoints for managing rentals.
| HTTP method |        Endpoint        |  Role   |           Description            |
|:-----------:|:----------------------:|:-------:|:--------------------------------:|
|    POST     |      `/rentals `       |   ALL   |        Save a new rental         |
|     GET     |    `/rentals/{id} `    | MANAGER |         Get rental by id         |
|     GET     | `/rentals/{id}/return` | MANAGER | Add actual return date to rental |
|     GET     |   `/rentals/search `   |   All   |  Get all rentals by parameters   |

---
### 👤 UserController - endpoints for managing users.
| HTTP method |      Endpoint       |  Role   |        Description         |
|:-----------:|:-------------------:|:-------:|:--------------------------:|
|     PUT     | `/users/{id}/role ` | MANAGER |    Update role for user    |
|     GET     |    `/users/me `     |   ALL   |    Get user information    |
|     PUT     |    `/users/me `     |   ALL   |  Update user information   |
___

___
## 🌟 Challenges and Solutions

One of the main challenges was to combine many components into a single, 
well-functioning system. This included ensuring that all technologies, 
such as the Telegram API and Stripe API, worked together smoothly. 
Realizing this goal required detailed planning, multiple testing iterations,
and a thoughtful approach. Thanks to my systematic approach and professional 
skills, I was able to successfully overcome these challenges and create 
a reliable and functional platform.

## 🌟 Possible improvements

- Enhanced User Experience: Optimize the interface for a seamless booking 
process and improve navigation for better usability.
- Advanced Booking Features: Introduce the ability to reserve specific car 
models and schedule pick-up/drop-off times with precision.
- Real-Time Tracking: Enable GPS tracking for rented vehicles to improve 
transparency and enhance safety.
- Flexible Payment Options: Integrate multiple payment gateways, including
Stripe API, for secure and diverse payment methods.
- Telegram Notifications: Leverage Telegram API to send real-time updates 
about booking status, reminders, and promotional offers.
- Subscription Plans: Offer subscription-based rentals for frequent 
users to reduce costs and simplify the process.
- Eco-Friendly Options: Expand the fleet to include electric and hybrid 
cars for environmentally conscious customers.
- Community Features: Add user reviews and ratings for cars to build trust
and assist new customers in making informed decisions. 

