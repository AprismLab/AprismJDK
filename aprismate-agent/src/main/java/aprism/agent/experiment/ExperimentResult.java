package aprism.agent.experiment;

/**
 * Outcome of a try-transform attempt.
 */
public sealed interface ExperimentResult permits
        ExperimentResult.Success, ExperimentResult.Failure {

    record Success(ExperimentHandle handle) implements ExperimentResult { }

    record Failure(String className, String errorType, String message)
            implements ExperimentResult { }

    static ExperimentResult ok(ExperimentHandle h) { return new Success(h); }
    static ExperimentResult fail(String cls, String type, String msg) { return new Failure(cls, type, msg); }

    default boolean isSuccess() {
        return this instanceof Success;
    }
}
