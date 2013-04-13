define ['scClientId'], (scClientId) ->
  # Update playlist if nothing playing & we have a queue
  updatePlaylist = () ->
    lastQueuedTrack = $(".queued").last()
    playingAnything = $(".playing").length > 0
    if lastQueuedTrack? and not playingAnything
      lastQueuedTrack.removeClass("queued").addClass("playing")
      playTrackMarkedForPlaying()

  # Play the track
  playTrackMarkedForPlaying = () ->
    toBePlayed = $(".playing")
    if toBePlayed?
      trackId = toBePlayed.find(".trackId").val()
      playTrackById(trackId)

  playTrackById = (trackId) ->
    SC.whenStreamingReady () -> SC.stream(trackId).play
      onfinish: () ->
        $(".playing").removeClass("playing").addClass("played")
        updatePlaylist()

  # Handle adding new track to the playlist
  $("form.add-track button").click( ->
    url = $("form.add-track input").val()
    resolveUrl = "http://api.soundcloud.com/resolve.json?url=" + url + "&client_id=" + scClientId
    $.ajax
      url: resolveUrl
      type: "GET"
      dataType: 'jsonp'
      crossDomain: true
      success: (data) ->
        trackInfo =
          track: data
          playedAt: "15:33"
        trackHtml = Mustache.render(MUSTACHE_TEMPLATES['track'], trackInfo)
        $("#playlist").prepend(trackHtml)
        updatePlaylist()
    false
  )

  # Initialize SounCloud API & bootstrap page
  SC.initialize(
    client_id: scClientId
  )
  playTrackMarkedForPlaying()