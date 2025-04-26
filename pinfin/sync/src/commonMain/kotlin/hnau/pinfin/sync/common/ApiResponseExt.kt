package hnau.pinfin.sync.common


inline fun <T, R> ApiResponse<T>.fold(
    ifSuccess: (T) -> R,
    ifError: (ApiError) -> R,
): R = when (this) {
    is ApiResponse.Error -> ifError(error)
    is ApiResponse.Success -> ifSuccess(data)
}

inline fun <I, O> ApiResponse<I>.map(
    transform: (I) -> O,
): ApiResponse<O> = fold(
    ifSuccess = { data ->
        val transformedData = transform(data)
        ApiResponse.Success(transformedData)
    },
    ifError = { error -> ApiResponse.Error(error) }
)