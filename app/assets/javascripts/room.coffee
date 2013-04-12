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
        console.log(data)
    false
  )