package io.linus.artemis.service.repository

import io.linus.artemis.persistence.entity.User

/* Repository pattern for business domain specification */
interface PosterRepository {
    fun listTopPoster(): List<User>
}

/* Ref: https://blog.sapiensworks.com/post/2012/11/01/Repository-vs-DAO.aspx */
/* Ref: http://blog.sapiensworks.com/post/2014/06/02/The-Repository-Pattern-For-Dummies.aspx */
/* Ref: https://stackoverflow.com/questions/31305199/repository-pattern-how-to-understand-it-and-how-does-it-work-with-complex-en */
/**
 * "Mediates between the domain and data mapping layers using a collection-like interface for accessing domain objects.
 * A system with a complex domain model often benefits from a layer [...]  that isolates domain objects from details of
 * the database access code"
 *
 * The main difference between a repository and a dao is that a repository returns only objects that are understood by
 * the calling layer. A repository makes more sense with 'rich' or at least properly encapsulated objects.
 *
 * A DAO is much closer to the underlying storage, it's really data centric. That's why in many cases DAOs are matching
 * db tables or views 1 on 1. A repository sits at a higher level. A repository deals with** business/domain objects **.
 * That's the difference. A repository will use a DAO to get the data from the storage and uses that data to restore a
 * business object. Or it will take a business object and extract the data that will be persisted. If one has an anemic
 * domain, the repository will be just a DAO. So the intention of the repository is to isolate the domain objects from
 * the database access concerns. For data-centric apps, a repository and DAO are interchangeable because the 'business'
 * objects are simple data.
 * */
