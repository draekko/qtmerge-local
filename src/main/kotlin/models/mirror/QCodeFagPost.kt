package models.mirror

data class QCodeFagPost(
        var host : String,
        var board : String,
        var id : String,
        var userId : String?,
        var timestamp : Long,
        var title : String?,
        var name : String?,
        var email : String?,
        var trip : String?,
        var text : String?,
        var subject : String?,
        var source : String,
        var link : String,
        var threadId : String?,
        var images : MutableList<QCodeFagPostImage>?,
        var references: MutableList<QCodeFagPost>
) {
    data class QCodeFagPostImage(
            var url : String?,
            var filename : String?
    )
}