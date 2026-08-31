package com.fitdroid.core.auth

class NotAuthorizedException : IllegalStateException("Google Health account is not linked")

class OAuthConfigException(message: String) : IllegalStateException(message)
