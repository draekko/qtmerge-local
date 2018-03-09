package models.mirror

data class FourChanThread(
    var op : FourChanPost?,
    var posts : Map<String, FourChanPost>?
)
