package io.apef.base.exception;

import static io.apef.base.exception.ErrSeverity.*;

/**
 * General ErrorCode used by Common Library
 */
public class GeneralErrorCodes {
    public final static StaticErrorCode INCOMPLETE_MSG = new StaticErrorCode(MODERATE, "0", "00001", "Incomplete message received.");
    public final static StaticErrorCode INVALID_MESSAGE = new StaticErrorCode(MODERATE, "0", "00002", "Message validation failed.");
    public final static StaticErrorCode REGISTRATION_OVERLAP = new StaticErrorCode(MODERATE, "0", "00003", "Registration overlap.");
    public final static StaticErrorCode QUERY_FAILED = new StaticErrorCode(MODERATE, "0", "00004", "Query failed.");
    public final static StaticErrorCode AMBIGUOUS_DETAILS = new StaticErrorCode(MODERATE, "0", "00005", "Ambiguous details.");
    public final static StaticErrorCode UNSUPPORTED_PROTOCOL = new StaticErrorCode(MODERATE, "0", "00006", "Unsupported protocol.");
    public final static StaticErrorCode ADDRESS_DOES_NOT_EXIST = new StaticErrorCode(MODERATE, "0", "00007", "Network address does not exist.");
    public final static StaticErrorCode IN_USE = new StaticErrorCode(MODERATE, "0", "00008", "In use.");
    public final static StaticErrorCode DUPLICATE_MSG_IDENTIFIER = new StaticErrorCode(MODERATE, "0", "00009", "Duplicate message identifier.");
    public final static StaticErrorCode CONN_LOST = new StaticErrorCode(MODERATE, "0", "00010", "Network connection lost.");
    public final static StaticErrorCode RESOURCE_NOT_FOUND = new StaticErrorCode(MODERATE, "0", "00011", "resource not found.");
    public final static StaticErrorCode NOT_SUPPORTED = new StaticErrorCode(MODERATE, "0", "00012", "Not supported.");
    public final static StaticErrorCode NOT_AUTHORIZED = new StaticErrorCode(MODERATE, "0", "00013", "Not authorized.");
    public final static StaticErrorCode UNKNOWN_MSG_REFERENCE = new StaticErrorCode(MODERATE, "0", "00014", "Unknown message reference.");
    public final static StaticErrorCode ABANDONED = new StaticErrorCode(MODERATE, "0", "00015", "Resend forced abandonment.");
    public final static StaticErrorCode OUT_OR_RESOURCES = new StaticErrorCode(MODERATE, "0", "00016", "Out of resources.");
    public final static StaticErrorCode TIMEOUT = new StaticErrorCode(MODERATE, "0", "00017", "Timeout.");
    public final static StaticErrorCode GENERAL = new StaticErrorCode(MODERATE, "0", "00018", "General error.");
    public final static StaticErrorCode MONGO = new StaticErrorCode(MODERATE, "0", "02200", "Mongo connection.");
}
