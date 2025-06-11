# MCP Client Demo

A Spring Boot application that demonstrates how to integrate with MCP (Model Calling Protocol) services discovered through Nacos, and interact with them using DeepSeek AI's LLM capabilities.

## Features

- Discovers MCP services and tools from Nacos
- Integrates with DeepSeek AI for natural language processing
- Provides both REST API and web interface
- Intelligent domain-based routing of user queries
- Tool selection based on description matching
- Allows domain-specific tool selection
- Maintains conversation context between messages
- Displays AI responses with realistic typing effect

## Requirements

- JDK 17 or higher
- Maven 3.6 or higher
- Nacos Server (for service discovery)
- DeepSeek API key

## Configuration

The application can be configured via `application.properties`:

```properties
# Application Configuration
spring.application.name=mcp-client-demo
server.port=8090

# Nacos Configuration
nacos.server-addr=127.0.0.1:8848
nacos.namespace=public
nacos.mcp.group=MCP_GROUP

# MCP Client Configuration
mcp.client.connection-timeout=3000
mcp.client.read-timeout=5000
mcp.client.domains=userCenter,tradeCenter,lgCenter,userQKCenter
mcp.client.refresh-interval-ms=30000

# DeepSeek AI Configuration
deepseek.api-key=your-deepseek-api-key
deepseek.base-url=https://api.deepseek.com
deepseek.model=deepseek-chat
deepseek.temperature=0.7
deepseek.max-tokens=4000

# Conversation Configuration
conversation.max-history-length=10

# Session Configuration
server.servlet.session.timeout=30m
```

## Getting Started

1. Make sure you have a running Nacos server with MCP services registered
2. Configure your DeepSeek API key in `application.properties`
3. Build the application:

```bash
mvn clean package
```

4. Run the application:

```bash
java -jar target/mcp-client-demo-0.0.1-SNAPSHOT.jar
```

5. Access the web interface at `http://localhost:8090`

## API Endpoints

### Chat API

```
POST /api/chat
```

Request body:
```json
{
  "message": "Your message here",
  "domain": "optional domain filter"
}
```

Response:
```json
{
  "message": "Response from the AI",
  "success": true
}
```

### Conversation Management

```
GET /api/conversation - Get conversation history for current session
POST /api/conversation/clear - Clear conversation history for current session
```

### Service Discovery APIs

```
GET /api/services - Get all discovered MCP services
GET /api/tools - Get all discovered MCP tools
GET /api/tools/{domain} - Get tools filtered by domain
POST /api/refresh - Force refresh of service discovery
```

## Architecture

This application follows a standard Spring Boot architecture:

- **Controllers**: Handle HTTP requests and responses
- **Services**: Contain business logic
- **Models**: Represent data structures
- **Clients**: Integrate with external systems (DeepSeek, Nacos)

The integration with DeepSeek AI is done via custom client implementation using Spring WebClient to communicate with the DeepSeek API.

### Domain-Based Routing

The application implements intelligent domain-based routing for user queries:

1. If a domain is explicitly specified in the request, it uses that domain
2. Otherwise, it attempts to determine the most relevant domain by:
   - Checking for direct mentions of domain names in the message
   - Scoring domains based on matches between the message and tool names/descriptions
   - Selecting the domain with the highest score

Once a domain is determined, the system gives priority to tools from that domain while still providing information about all available tools across domains.

### Tool Selection

Tool selection is based on matching the user's query with tool descriptions:

1. Tools are organized by domain
2. The LLM is instructed to first identify the most relevant domain
3. Then select specific tools based on their descriptions
4. For queries spanning multiple domains, the LLM can recommend tools from each relevant domain

### Conversation History

The application maintains conversation history per session, allowing the AI to have context of previous interactions. This is implemented using:

- ConversationService: Tracks messages for each user session
- Session-based storage: Uses Spring's HttpSession for identifying users
- Configurable history length: Controls how many messages are kept in context

### Typing Effect

The AI responses are displayed with a realistic typing effect:

- Character-by-character typing animation
- Typing indicator while processing
- Adjustable typing speed

## License

This project is licensed under the MIT License. # mcp-client-demo
