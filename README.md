# Study.io


## Backend layers
1. Db layer
    - dao layer
        - Defines an interface for interacting with the db 
        - Basically sql abstracts sql ops into functions we can use
     
    - database layer(we still need to decide what type of db)
        - Define the entire db here - all tables
        - Define the entities, the version and the methods to access the dao
        - Entrypoint for all db operations
    - module layer(for dependency injection)
        - Provides the db, dao and repo layers to the rest of the app

2. Model layer
    - Define class for the schema

3. Repository layer
    - Provide methods for viewmodel layer to interface with dao'

4. viewmodel layer
    - Provide methods for frontend
    - Calls repo layer
    - Holds the ui state

UI (Jetpack Compose)
   ↑
ViewModel
   ↑
Repository
   ↑
DAO  ←→ DB
   ↑
@Module (Hilt provides dependencies)


    
