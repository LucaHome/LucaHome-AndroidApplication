package guepardoapps.lucahome.common.models.wirelesssocket

import guepardoapps.lucahome.common.annotations.JsonKey
import guepardoapps.lucahome.common.annotations.NeededNetwork
import guepardoapps.lucahome.common.annotations.NeededUserRole
import guepardoapps.lucahome.common.enums.common.NetworkType
import guepardoapps.lucahome.common.enums.common.ServerAction
import guepardoapps.lucahome.common.enums.common.ServerDatabaseAction
import guepardoapps.lucahome.common.enums.user.UserRole
import java.io.Serializable
import java.util.*

@JsonKey("Data", "WirelessSocket")
class WirelessSocket : Serializable {
    private val tag: String = WirelessSocket::class.java.simpleName

    @JsonKey("", "Uuid")
    var uuid: UUID = UUID.randomUUID()

    @JsonKey("", "RoomUuid")
    var roomUuid: UUID = UUID.randomUUID()

    @JsonKey("", "Name")
    var name: String = ""

    @JsonKey("", "Code")
    var code: String = ""

    @JsonKey("", "State")
    var state: Boolean = false

    @JsonKey("LastTrigger", "DateTime")
    var lastTriggerDateTime: Calendar = Calendar.getInstance()

    @JsonKey("LastTrigger", "User")
    var lastTriggerUser: String = ""

    var isOnServer: Boolean = true
    var serverDatabaseAction: ServerDatabaseAction = ServerDatabaseAction.Null
    var changeCount: Int = 0
    var showInNotification: Boolean = true

    @NeededUserRole(UserRole.Guest)
    @NeededNetwork(NetworkType.HomeWifi)
    val commandSetState: String = "${ServerAction.WirelessSocketSet.command}$uuid&state=${if (state) "1" else "0"}"

    @NeededUserRole(UserRole.User)
    @NeededNetwork(NetworkType.HomeWifi)
    val commandAdd: String = "${ServerAction.WirelessSocketAdd.command}$uuid&roomuuid=$roomUuid&name=$name&code=$code"

    @NeededUserRole(UserRole.User)
    @NeededNetwork(NetworkType.HomeWifi)
    val commandUpdate: String = "${ServerAction.WirelessSocketUpdate.command}$uuid&roomuuid=$roomUuid&name=$name&code=$code&isactivated=${if (state) "1" else "0"}"

    @NeededUserRole(UserRole.Administrator)
    @NeededNetwork(NetworkType.HomeWifi)
    val commandDelete: String = "${ServerAction.WirelessSocketDelete.command}$uuid"

    override fun toString(): String {
        return "{" +
                "\"Class\":\"$tag\"," +
                "\"Uuid\":\"$uuid\"," +
                "\"RoomUuid\":\"$roomUuid\"," +
                "\"Name\":\"$name\"," +
                "\"Code\":\"$code\"," +
                "\"State\":\"${if (state) "1" else "0"}\"" +
                "}"
    }
}
