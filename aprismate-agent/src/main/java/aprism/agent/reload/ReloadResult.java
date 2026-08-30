package aprism.agent.reload;

import java.util.List;

/**
 * Result of applying a ChangeSet.
 */
public sealed interface ReloadResult permits
        ReloadResult.Success, ReloadResult.Failure, ReloadResult.ValidationFailed {

    record Success(List<ChangeSetValidator.ValidationResult> validations) implements ReloadResult { }

    record Failure(String reason) implements ReloadResult { }

    record ValidationFailed(List<ChangeSetValidator.ValidationResult> validations) implements ReloadResult { }

    static ReloadResult success(List<ChangeSetValidator.ValidationResult> v) { return new Success(v); }
    static ReloadResult failure(String reason) { return new Failure(reason); }
    static ReloadResult validationFailed(List<ChangeSetValidator.ValidationResult> v) { return new ValidationFailed(v); }

    default boolean isSuccess() { return this instanceof Success; }
}
