# Study.io - Component Diagram

## System Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              STUDY.IO SYSTEM ARCHITECTURE                        │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────┐
│                              PRESENTATION LAYER                                 │
│                              (Android Device)                                   │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐            │
│  │   HomeScreen    │    │  LoggedInScreen │    │SelectedClassScreen│           │
│  │   (Composable)  │    │   (Composable)  │    │   (Composable)  │            │
│  └─────────────────┘    └─────────────────┘    └─────────────────┘            │
│           │                       │                       │                    │
│           │                       │                       │                    │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐            │
│  │   LoginComponent│    │ ClassListComponent│   │  CardListScreen │            │
│  │   (Composable)  │    │   (Composable)  │    │   (Composable)  │            │
│  └─────────────────┘    └─────────────────┘    └─────────────────┘            │
│           │                       │                       │                    │
│           │                       │                       │                    │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐            │
│  │  SignUpComponent│    │   MainNavigation│    │   Theme/Colors  │            │
│  │   (Composable)  │    │   (Navigation)  │    │   (UI Assets)   │            │
│  └─────────────────┘    └─────────────────┘    └─────────────────┘            │
│                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    │ Navigation Events
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              VIEWMODEL LAYER                                   │
│                              (Android Device)                                   │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐            │
│  │ ClassListViewModel│   │  DeckViewModel  │    │  CardViewModel  │            │
│  │   (State Mgmt)  │    │   (State Mgmt)  │    │   (State Mgmt)  │            │
│  └─────────────────┘    └─────────────────┘    └─────────────────┘            │
│           │                       │                       │                    │
│           │                       │                       │                    │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐            │
│  │   getClasses()  │    │   decks State   │    │   cards State   │            │
│  │   (Placeholder) │    │   (Observable)  │    │   (Observable)  │            │
│  └─────────────────┘    └─────────────────┘    └─────────────────┘            │
│                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    │ Repository Calls
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              REPOSITORY LAYER                                  │
│                              (Android Device)                                   │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐            │
│  │ DeckRepository  │    │ReviewCardRepository│  │   (Future:     │            │
│  │   (Business     │    │   (Business     │    │  UserRepository)│            │
│  │    Logic)       │    │    Logic)       │    │                │            │
│  └─────────────────┘    └─────────────────┘    └─────────────────┘            │
│           │                       │                                            │
│           │                       │                                            │
│  ┌─────────────────┐    ┌─────────────────┐                                    │
│  │ insertDummyData()│   │insertDummyCards()│                                   │
│  │ getAllDecks()   │    │getAllReviewCards()│                                  │
│  └─────────────────┘    └─────────────────┘                                    │
│                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    │ DAO Operations
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              DATA ACCESS LAYER                                 │
│                              (Android Device)                                   │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐            │
│  │    DeckDao      │    │  ReviewCardsDao │    │   AppDatabase   │            │
│  │   (Interface)   │    │   (Interface)   │    │   (Room DB)     │            │
│  └─────────────────┘    └─────────────────┘    └─────────────────┘            │
│           │                       │                       │                    │
│           │                       │                       │                    │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐            │
│  │ CRUD Operations │    │ CRUD Operations │    │ SQLite Database │            │
│  │ (Insert, Query) │    │ (Insert, Query) │    │ (Local Storage) │            │
│  └─────────────────┘    └─────────────────┘    └─────────────────┘            │
│                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    │ Entity Storage
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              DATA MODEL LAYER                                  │
│                              (Android Device)                                   │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐            │
│  │      Deck       │    │   ReviewCards   │    │   (Future:      │            │
│  │   (Entity)      │    │    (Entity)     │    │    User)        │            │
│  └─────────────────┘    └─────────────────┘    └─────────────────┘            │
│           │                       │                                            │
│           │                       │                                            │
│  ┌─────────────────┐    ┌─────────────────┐                                    │
│  │ id, name, desc  │    │id, deckId, front│                                    │
│  │ cardCount, color│    │back, difficulty │                                    │
│  └─────────────────┘    └─────────────────┘                                    │
│                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    │ Dependency Injection
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              DEPENDENCY INJECTION                               │
│                              (Android Device)                                   │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐            │
│  │   AppModule     │    │ StudyIoApplication│   │   Hilt DI      │            │
│  │   (Hilt Module) │    │   (Application) │    │   (Container)   │            │
│  └─────────────────┘    └─────────────────┘    └─────────────────┘            │
│           │                       │                       │                    │
│           │                       │                       │                    │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐            │
│  │ Provides DB     │    │ Lifecycle Mgmt  │    │ Dependency      │            │
│  │ Provides DAOs   │    │ Hilt Setup      │    │ Resolution      │            │
│  └─────────────────┘    └─────────────────┘    └─────────────────┘            │
│                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────┘

## Component Annotations

### Physical Device Mapping:
- **Android Device**: All components execute on the user's Android smartphone/tablet
- **Local Storage**: SQLite database stored on device's internal storage
- **UI Rendering**: Jetpack Compose renders UI components on device screen
- **Background Processing**: Coroutines handle async operations on device CPU

### Key Connectors:
1. **Navigation Events**: Compose Navigation handles screen transitions
2. **State Updates**: ViewModels communicate state changes to UI
3. **Repository Calls**: ViewModels invoke repository methods for data operations
4. **DAO Operations**: Repositories use DAO interfaces for database access
5. **Dependency Injection**: Hilt provides component dependencies at runtime

### Data Flow:
1. User interacts with UI components
2. UI components trigger ViewModel methods
3. ViewModels call Repository methods
4. Repositories use DAOs to access database
5. Database operations update local SQLite storage
6. State changes flow back through the layers to update UI

### Future Components (Planned):
- **User Authentication**: Firebase Auth or custom auth system
- **Cloud Sync**: Firebase Firestore or custom backend
- **Push Notifications**: Firebase Cloud Messaging
- **Analytics**: Study progress tracking and statistics
- **Export/Import**: Deck sharing and backup functionality
```
