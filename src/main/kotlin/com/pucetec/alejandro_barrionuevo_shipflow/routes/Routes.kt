package com.pucetec.alejandro_barrionuevo_shipflow.routes

object Routes {
    const val BASE_URL = "/shipflow/api"

    const val PACKAGES = "$BASE_URL/packages"
    const val PACKAGE_BY_ID = "$PACKAGES/{trackingId}"
    const val PACKAGE_STATUS = "$PACKAGES/{trackingId}/status"
    const val PACKAGE_HISTORY = "$PACKAGES/{trackingId}/history"
}