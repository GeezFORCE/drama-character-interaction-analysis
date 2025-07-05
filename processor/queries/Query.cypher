// Get Characters in a given drama
MATCH path = (d:Drama {title: "Darius"})-[:HAS_CHARACTER]->()
RETURN path