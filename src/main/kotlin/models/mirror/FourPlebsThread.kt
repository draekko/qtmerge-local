package models.mirror

data class FourPlebsThread(
        var op : FourPlebsPost?,
        var posts : Map<String, FourPlebsPost>?
)
