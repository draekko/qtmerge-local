package models.mirror



data class FourPlebsBoardActivity(
        var statistic : ActivityStastic,
        var data : ActivityData
) {
    data class ActivityStastic(
        var name : String,
        var timestamp : String
    )

    data class ActivityDataPage(
        var time : String,
        var posts : String,
        var images : String,
        var sage : String
    )

    data class ActivityData(
        var board : Array<ActivityDataPage>,
        var ghost : Array<ActivityDataPage>,
        var karma : Array<ActivityDataPage>,
        var total : Array<ActivityDataPage>
    )
}