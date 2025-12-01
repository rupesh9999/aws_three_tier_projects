# Contributing to StreamFlix

Thank you for your interest in contributing to StreamFlix! This document provides guidelines and information for contributors.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Workflow](#development-workflow)
- [Coding Standards](#coding-standards)
- [Pull Request Process](#pull-request-process)
- [Testing Guidelines](#testing-guidelines)
- [Documentation](#documentation)

## Code of Conduct

This project adheres to a Code of Conduct. By participating, you are expected to uphold this code:

- Be respectful and inclusive
- Welcome newcomers and help them get started
- Focus on what is best for the community
- Show empathy towards other community members

## Getting Started

### Prerequisites

- Node.js 20.x LTS
- Java 21 (Temurin/OpenJDK recommended)
- Docker Desktop
- Git
- Make

### Setting Up the Development Environment

1. **Fork the repository**
   ```bash
   # Clone your fork
   git clone https://github.com/YOUR_USERNAME/streamflix.git
   cd streamflix
   
   # Add upstream remote
   git remote add upstream https://github.com/streamflix/streamflix.git
   ```

2. **Install dependencies**
   ```bash
   # Frontend
   cd frontend && npm install
   
   # Backend (builds all modules)
   cd ../backend && ./mvnw clean install
   ```

3. **Start development environment**
   ```bash
   make dev-up
   ```

## Development Workflow

### Branch Naming Convention

- `feature/` - New features (e.g., `feature/user-profile-avatars`)
- `bugfix/` - Bug fixes (e.g., `bugfix/login-redirect-loop`)
- `hotfix/` - Critical production fixes (e.g., `hotfix/payment-processing`)
- `docs/` - Documentation changes (e.g., `docs/api-documentation`)
- `refactor/` - Code refactoring (e.g., `refactor/auth-service-cleanup`)

### Commit Message Format

We follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <subject>

[optional body]

[optional footer(s)]
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Adding or modifying tests
- `chore`: Maintenance tasks
- `perf`: Performance improvements
- `ci`: CI/CD changes

**Examples:**
```
feat(auth): add MFA support with TOTP

Implement time-based one-time password authentication
as a second factor for user login.

Closes #123
```

```
fix(playback): resolve buffering issue on slow connections

- Adjust adaptive bitrate thresholds
- Add retry logic for failed segments
- Improve error handling

Fixes #456
```

## Coding Standards

### Java (Backend)

- Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Use Lombok annotations to reduce boilerplate
- Write meaningful Javadoc for public APIs
- Maximum line length: 120 characters

```java
// Good
@Service
@RequiredArgsConstructor
@Slf4j
public class ContentService {
    
    private final ContentRepository contentRepository;
    
    /**
     * Retrieves content by ID.
     *
     * @param id the content ID
     * @return the content, or empty if not found
     */
    public Optional<Content> findById(UUID id) {
        log.debug("Finding content by id: {}", id);
        return contentRepository.findById(id);
    }
}
```

### TypeScript (Frontend)

- Follow [Airbnb TypeScript Style Guide](https://github.com/airbnb/javascript)
- Use functional components with hooks
- Prefer named exports over default exports
- Use TypeScript strict mode

```typescript
// Good
interface ContentCardProps {
  content: Content;
  onSelect: (id: string) => void;
}

export const ContentCard: React.FC<ContentCardProps> = ({ content, onSelect }) => {
  const handleClick = useCallback(() => {
    onSelect(content.id);
  }, [content.id, onSelect]);

  return (
    <div className="content-card" onClick={handleClick}>
      <img src={content.thumbnailUrl} alt={content.title} />
      <h3>{content.title}</h3>
    </div>
  );
};
```

### SQL Migrations

- Use Flyway naming convention: `V{version}__{description}.sql`
- Always include rollback plan in comments
- Use snake_case for table and column names

```sql
-- V2__add_user_preferences.sql
-- Rollback: DROP TABLE IF EXISTS user_preferences;

CREATE TABLE user_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    theme VARCHAR(20) DEFAULT 'dark',
    language VARCHAR(10) DEFAULT 'en',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_preferences_user_id ON user_preferences(user_id);
```

## Pull Request Process

1. **Before submitting:**
   - Ensure all tests pass: `make test`
   - Run linting: `make lint`
   - Update documentation if needed
   - Rebase on latest `main`

2. **PR Title Format:**
   ```
   [TYPE] Brief description
   ```
   Example: `[FEATURE] Add user profile avatars`

3. **PR Description Template:**
   ```markdown
   ## Description
   Brief description of changes

   ## Type of Change
   - [ ] Bug fix
   - [ ] New feature
   - [ ] Breaking change
   - [ ] Documentation update

   ## Testing
   - [ ] Unit tests pass
   - [ ] Integration tests pass
   - [ ] Manual testing completed

   ## Screenshots (if applicable)

   ## Checklist
   - [ ] Code follows style guidelines
   - [ ] Self-review completed
   - [ ] Comments added for complex code
   - [ ] Documentation updated
   - [ ] No new warnings introduced
   ```

4. **Review Process:**
   - At least 1 approval required
   - All CI checks must pass
   - No unresolved conversations

## Testing Guidelines

### Backend Testing

```java
@SpringBootTest
@Testcontainers
class ContentServiceTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @Autowired
    private ContentService contentService;

    @Test
    void shouldCreateContent() {
        // Given
        CreateContentRequest request = CreateContentRequest.builder()
            .title("Test Movie")
            .type(ContentType.MOVIE)
            .build();

        // When
        Content result = contentService.create(request);

        // Then
        assertThat(result.getId()).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Movie");
    }
}
```

### Frontend Testing

```typescript
import { render, screen, fireEvent } from '@testing-library/react';
import { ContentCard } from './ContentCard';

describe('ContentCard', () => {
  const mockContent = {
    id: '1',
    title: 'Test Movie',
    thumbnailUrl: 'https://example.com/thumb.jpg',
  };

  it('should render content title', () => {
    render(<ContentCard content={mockContent} onSelect={jest.fn()} />);
    expect(screen.getByText('Test Movie')).toBeInTheDocument();
  });

  it('should call onSelect when clicked', () => {
    const onSelect = jest.fn();
    render(<ContentCard content={mockContent} onSelect={onSelect} />);
    
    fireEvent.click(screen.getByRole('img'));
    expect(onSelect).toHaveBeenCalledWith('1');
  });
});
```

### Test Coverage Requirements

- Backend: Minimum 80% line coverage
- Frontend: Minimum 70% line coverage
- Critical paths: 100% coverage

## Documentation

### API Documentation

- Document all REST endpoints with OpenAPI annotations
- Include request/response examples
- Document error responses

```java
@Operation(summary = "Get content by ID")
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Content found"),
    @ApiResponse(responseCode = "404", description = "Content not found")
})
@GetMapping("/{id}")
public ResponseEntity<ContentResponse> getById(@PathVariable UUID id) {
    // ...
}
```

### Code Comments

- Write self-documenting code
- Add comments for complex logic
- Document "why", not "what"

```java
// Good: Explains the business reason
// Using exponential backoff to prevent overwhelming the payment provider
// during peak traffic periods
int delay = (int) Math.pow(2, retryAttempt) * 1000;

// Bad: States the obvious
// Increment counter by 1
counter++;
```

## Questions?

If you have questions, please:

1. Check existing issues and discussions
2. Join our community chat
3. Open a new discussion on GitHub

Thank you for contributing! 🎉
