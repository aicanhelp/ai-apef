package io.apef.base.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum VexExceptions {
    E_302(new VexException(302, "Redirect")),
    E_400(new VexException(400, "400 Bad Request")),
    E_401(new VexException(401, "Unauthorized")),
    E_402(new VexException(402, "Payment Required")),
    E_403(new VexException(403, "Forbidden")),
    E_404(new VexException(404, "Not Found")),
    E_405(new VexException(405, "Method Not Allowed")),
    E_406(new VexException(406, "Not Acceptable")),
    E_407(new VexException(407, "Proxy Authentication Required")),
    E_408(new VexException(408, "Request Timeout")),
    E_409(new VexException(409, "Conflict")),
    E_410(new VexException(410, "Gone")),
    E_411(new VexException(411, "Length Required")),
    E_412(new VexException(412, "Precondition Failed")),
    E_413(new VexException(413, "Request Entity Too Large")),
    E_414(new VexException(414, "Request-URI Too Long")),
    E_415(new VexException(415, "Unsupported Media Type")),
    E_416(new VexException(416, "Requested Range Not Satisfiable")),
    E_417(new VexException(417, "Expectation Failed")),
    E_422(new VexException(422, "Unprocessable Entity")),
    E_423(new VexException(423, "Locked")),
    E_424(new VexException(424, "Failed Dependency")),
    E_425(new VexException(425, "Unordered Collection")),
    E_426(new VexException(426, "Upgrade Required")),
    E_500(new VexException(500, "Internal Server Error")),
    E_501(new VexException(501, "Not Implemented")),
    E_502(new VexException(502, "Bad Gateway")),
    E_503(new VexException(503, "Service Unavailable")),
    E_504(new VexException(504, "Gateway Timeout")),
    E_505(new VexException(505, "HTTP Version Not Supported")),
    E_506(new VexException(506, "Variant Also Negotiates")),
    E_507(new VexException(507, "Insufficient Storage")),
    E_509(new VexException(509, "Bandwidth Limit Exceeded")),
    E_510(new VexException(510, "Not Extended")),
    E_600(new VexException(600, "Unparseable Response Headers")),
    E_701(new VexException(701, 400, "Codec Not Match")),
    E_702(new VexException(702, 404, "Max Retried")),
    E_000(new VexException(-1, 500, "Unknown"));

    private VexException exception;

    private static Map<Integer, VexException> values = new HashMap<>();

    static {
        for (VexExceptions vexExceptions : VexExceptions.values()) {
            values.put(vexExceptions.exception.errCode(), vexExceptions.exception);
        }
    }

    public static String statusMessage(int errCode) {
        return of(errCode).getMessage();
    }

    public static VexException of(int errCode) {
        return values.getOrDefault(errCode, E_000.exception);
    }

    public boolean equals(Throwable throwable) {
        if (!(throwable instanceof VexException)) return false;
        return ((VexException) throwable).errCode() == this.exception().errCode();
    }

    /**
     * NOTE: poor performance
     *
     * @param throwable
     * @return
     */
    public static VexException of(Throwable throwable) {
        if (throwable == null) return E_500.exception;
        if (throwable instanceof VexException) return (VexException) throwable;
        return new VexException(throwable);
    }

    public static boolean isMaxRetried(Throwable throwable) {
        return (throwable == E_702.exception);
    }

    public static boolean isCodecNotMatch(Throwable throwable) {
        return throwable == E_701.exception;
    }

    public static boolean isRedirect(Throwable throwable) {
        return throwable == E_302.exception;
    }
}
