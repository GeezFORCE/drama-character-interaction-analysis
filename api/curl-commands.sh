# ---------------- #
# ---- DraCor ---- #
# ---------------- #

# Get details of all english dramas in the Dracor corpus
curl --request GET \
  --url https://dracor.org/api/v1/corpora/eng

# Get TEI data of a specific play using the "play ID"
curl --request GET \
  --url https://dracor.org/api/v1/corpora/eng/plays/<play-id>/tei

# ---------------- #
# ---- Scraper --- #
# ---------------- #

# Load specific plays
curl --request PUT \
  --url http://localhost:8081/load \
  --header 'content-type: application/json' \
  --data '[
  "udall-ralph-roister-doister",
  "stevenson-gammer-gurton-s-needle"
]'

# Load all plays - WOULD TAKE TIME!!
curl --request PUT \
  --url http://localhost:8081/load/all

# ---------------- #
# --- eXist-DB --- #
# ---------------- #

# Details of all plays in Exist-DB
curl --request GET \
  --url http://localhost:8080/exist/rest/db/plays \
  --header 'authorization: Basic YWRtaW46'

# Get the XML data of a play
curl --request GET \
  --url http://localhost:8080/exist/rest/db/plays/<play-name>.xml \
  --header 'authorization: Basic YWRtaW46' \
  --header 'content-type: application/xml'