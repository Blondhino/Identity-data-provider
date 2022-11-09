package com.enioob.identity_data_provider.model

import com.enioob.identity_data_provider.com.enioob.identity_data_provider.fragment.UserFragment

data class IdpUser(
  val id: String = "",
  val createdAt: String = "",
  val updatedAt: String = "",
  val email: String = "",
  val phone: String = "",
  val name: String = "",
  val nickname: String = "",
  val profileImageUrl: String = "",
  val roles: String = "",
  val verificationStatus: String = "",
)


fun UserFragment.toIdpUser(): IdpUser {
  return IdpUser(
    id = this.id,
    createdAt = this.created_at,
    updatedAt = this.updated_at,
    email = this.email ?: "",
    phone = this.phone ?: "",
    name = this.name ?: "",
    nickname = this.nick_name ?: "",
    profileImageUrl = this.avatar_url ?: "",
    roles = this.claims ?: "",
    verificationStatus = this.status.name
  )
}
