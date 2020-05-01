package io.apef.repository;


import io.apef.base.exception.ErrSeverity;
import io.apef.base.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public enum RepositoryError implements ErrorCode {
    General(ErrSeverity.MILD, "9", "0000", "Repository General Error."),
    WARN(ErrSeverity.MILD, "9", "0000", "Repository Warning.");;

    private ErrSeverity severity;
    private String appCode, errCode, message;
}