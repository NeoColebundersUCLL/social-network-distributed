package be.ucll.da.postservice.domain.post;

public enum PostStatus {
    // happy
    REGISTERED,
    VALIDATING_TAGGED_USERS,
    VALIDATING_OWNER,

    // failure
    USERS_NOT_VALID,
    NO_OWNER,

    // end states
    ACCEPTED
}
