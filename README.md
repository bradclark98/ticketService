# ticketService
A simple Ticket Service that handles, ticket requests, ticket reservations and number of available tickets.  I


Assumption
    When implementing best available ticket, I assumed that best would be determined by the ticket quality, row and seat number in that order.  My assumption is that lower rows and seats numbers are considered higher quality, seat quality is very venue dependant and somewhat subjective.  I added a seatQuality variable and used row and seat number as tie breakers.
    

To build and run test cases 
    mvn clean install
