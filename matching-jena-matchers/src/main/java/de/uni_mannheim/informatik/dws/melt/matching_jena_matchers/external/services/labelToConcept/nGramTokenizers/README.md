# One-to-Many Concept Linking Strategies: Tokenization

Problem Statement: Some entities to be linked contain multiple concepts at once.
An example would be a named relation between two entities like 
`CounterpartyOfFinancialContract` which links entitiy `BusinessPartner` and entity
`Financial Contract`. It is likely that `CounterpartyOfFinancialContract` is not
found as one existing concept. Therefore, you can configure how to handle
such cases by defining the kind of tokenization you deem meaningful for 
your usecase. Ideally, your strategy links the relation to conepts
`Counterparty` and `Financial Contract`.<br/>

The classes contained in this package help you to do so. The linkers are configured in a way so that by default a good tokenization strategy is selected.