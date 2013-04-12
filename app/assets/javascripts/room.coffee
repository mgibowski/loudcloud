define ['scClientId'], (scClientId) ->
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
        $("#playlist").append(trackHtml)
    false
  )