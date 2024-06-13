package ru.snapix.clan

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import ru.snapix.clan.api.ClanRole

object ClanRoleSerializer : KSerializer<ClanRole> {
    override val descriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder): ClanRole = ClanRole.role(decoder.decodeString())
    override fun serialize(encoder: Encoder, value: ClanRole) = encoder.encodeString(value.name)
}