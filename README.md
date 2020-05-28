# Consumer Application

This application is an implementation of a RabbitMQ consumer application, that runs indefinitely and serializes the Rundeck job requests from the client application. This demonstrates one of the uses of message queue.

This implementation is used specifically in APKGen to avoid server crash on multiple user requests.  

