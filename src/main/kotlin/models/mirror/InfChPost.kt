package models.mirror

data class InfChPostExtraFile(
    var tn_h : Int,
    var tn_w : Int,
    var h : Int,
    var w : Int,
    var fsize : Long,
    var filename : String,
    var ext : String,
    var tim : String,
    var fpath : Int,
    var spoiler : Int,
    var md5 : String
)

data class InfChPost(
        var no : Long,
        var title : String,
        var sub : String,
        var com : String,
        var name : String,
        var email : String,
        var trip : String,
        var time : Long,
        var omitted_posts : Int,
        var omitted_images : Int,
        var sticky : Int,
        var locked : Int,
        var cyclical : String,
        var bumplocked : String,
        var last_modified : Long,
        var id : String,
        var embed_thumb : String,
        var tn_h : Int,
        var tn_w : Int,
        var h : Int,
        var w : Int,
        var fsize : Long,
        var filename : String?,
        var ext : String?,
        var tim : String?,
        var fpath : Int,
        var spoiler : Int,
        var md5 : String,
        var extra_files : Array<InfChPostExtraFile>?,
        var resto : Int
)