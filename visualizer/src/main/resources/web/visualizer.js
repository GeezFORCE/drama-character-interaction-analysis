// Drama Character Interaction Visualizer

// Global variables
let svg;
let simulation;
let dramaSelect = document.getElementById('drama-select');
let characterInfo = document.getElementById('character-info');
let tooltip;

// Initialize the visualization
document.addEventListener('DOMContentLoaded', function() {
    // Create SVG container
    svg = d3.select('#graph')
        .append('svg')
        .attr('width', '100%')
        .attr('height', '100%');
    
    // Create tooltip
    tooltip = d3.select('body')
        .append('div')
        .attr('class', 'tooltip')
        .style('opacity', 0);
    
    // Load drama list
    loadDramas();
    
    // Add event listener to drama select
    dramaSelect.addEventListener('change', function() {
        const selectedDrama = this.value;
        if (selectedDrama) {
            loadGraph(selectedDrama);
        } else {
            clearGraph();
        }
    });
});

// Load list of dramas from API
function loadDramas() {
    fetch('/api/graph/dramas')
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to load dramas');
            }
            return response.json();
        })
        .then(dramas => {
            // Clear existing options except the first one
            while (dramaSelect.options.length > 1) {
                dramaSelect.remove(1);
            }
            
            // Add new options
            dramas.forEach(drama => {
                const option = document.createElement('option');
                option.value = drama;
                option.textContent = drama;
                dramaSelect.appendChild(option);
            });
        })
        .catch(error => {
            console.error('Error loading dramas:', error);
            alert('Failed to load drama list. Please try again later.');
        });
}

// Load graph data for a specific drama
function loadGraph(dramaTitle) {
    fetch(`/api/graph/drama/${encodeURIComponent(dramaTitle)}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to load graph data');
            }
            return response.json();
        })
        .then(data => {
            renderGraph(data);
        })
        .catch(error => {
            console.error('Error loading graph data:', error);
            alert('Failed to load graph data. Please try again later.');
        });
}

// Render the graph using D3.js
function renderGraph(data) {
    // Clear existing graph
    clearGraph();
    
    // Get SVG dimensions
    const width = svg.node().getBoundingClientRect().width;
    const height = svg.node().getBoundingClientRect().height;
    
    // Create force simulation
    simulation = d3.forceSimulation(data.nodes)
        .force('link', d3.forceLink(data.links).id(d => d.id).distance(100))
        .force('charge', d3.forceManyBody().strength(-300))
        .force('center', d3.forceCenter(width / 2, height / 2))
        .force('collision', d3.forceCollide().radius(30));
    
    // Create links
    const link = svg.append('g')
        .attr('class', 'links')
        .selectAll('line')
        .data(data.links)
        .enter()
        .append('line')
        .attr('class', 'link')
        .style('stroke-width', d => Math.sqrt(d.value));
    
    // Create nodes
    const node = svg.append('g')
        .attr('class', 'nodes')
        .selectAll('circle')
        .data(data.nodes)
        .enter()
        .append('circle')
        .attr('class', d => `node ${d.gender.toLowerCase()}`)
        .attr('r', 10)
        .call(d3.drag()
            .on('start', dragstarted)
            .on('drag', dragged)
            .on('end', dragended));
    
    // Add node labels
    const labels = svg.append('g')
        .attr('class', 'labels')
        .selectAll('text')
        .data(data.nodes)
        .enter()
        .append('text')
        .text(d => d.name)
        .attr('x', 12)
        .attr('y', 3);
    
    // Add node interactions
    node.on('mouseover', function(event, d) {
        // Show tooltip
        tooltip.transition()
            .duration(200)
            .style('opacity', .9);
        tooltip.html(`<strong>${d.name}</strong><br>Gender: ${d.gender}`)
            .style('left', (event.pageX + 10) + 'px')
            .style('top', (event.pageY - 28) + 'px');
        
        // Highlight connected nodes and links
        const connectedNodeIds = data.links
            .filter(link => link.source.id === d.id || link.target.id === d.id)
            .flatMap(link => [link.source.id, link.target.id]);
        
        node.style('opacity', n => connectedNodeIds.includes(n.id) || n.id === d.id ? 1 : 0.2);
        link.style('opacity', l => l.source.id === d.id || l.target.id === d.id ? 1 : 0.1);
        labels.style('opacity', n => connectedNodeIds.includes(n.id) || n.id === d.id ? 1 : 0.2);
    })
    .on('mouseout', function() {
        // Hide tooltip
        tooltip.transition()
            .duration(500)
            .style('opacity', 0);
        
        // Reset highlight
        node.style('opacity', 1);
        link.style('opacity', 1);
        labels.style('opacity', 1);
    })
    .on('click', function(event, d) {
        // Show character info
        showCharacterInfo(d, data);
    });
    
    // Update positions on simulation tick
    simulation.on('tick', () => {
        link
            .attr('x1', d => d.source.x)
            .attr('y1', d => d.source.y)
            .attr('x2', d => d.target.x)
            .attr('y2', d => d.target.y);
        
        node
            .attr('cx', d => d.x)
            .attr('cy', d => d.y);
        
        labels
            .attr('x', d => d.x + 12)
            .attr('y', d => d.y + 3);
    });
}

// Show character information in the info panel
function showCharacterInfo(character, data) {
    // Find all interactions for this character
    const interactions = data.links.filter(link => 
        link.source.id === character.id || link.target.id === character.id
    );
    
    // Sort interactions by value (interaction count)
    interactions.sort((a, b) => b.value - a.value);
    
    // Create HTML for character info
    let html = `
        <h3>${character.name}</h3>
        <p><strong>Gender:</strong> ${character.gender}</p>
        <p><strong>Total Interactions:</strong> ${interactions.length}</p>
        
        <h4>Interactions:</h4>
        <ul>
    `;
    
    // Add interactions to HTML
    interactions.forEach(interaction => {
        const otherCharacter = interaction.source.id === character.id 
            ? interaction.target.name 
            : interaction.source.name;
        
        html += `<li>${otherCharacter} (${interaction.value} interactions)</li>`;
    });
    
    html += '</ul>';
    
    // Update character info panel
    characterInfo.innerHTML = html;
}

// Clear the graph
function clearGraph() {
    if (simulation) {
        simulation.stop();
    }
    svg.selectAll('*').remove();
    characterInfo.innerHTML = '<p>Select a character in the graph to see details.</p>';
}

// Drag functions for nodes
function dragstarted(event, d) {
    if (!event.active) simulation.alphaTarget(0.3).restart();
    d.fx = d.x;
    d.fy = d.y;
}

function dragged(event, d) {
    d.fx = event.x;
    d.fy = event.y;
}

function dragended(event, d) {
    if (!event.active) simulation.alphaTarget(0);
    d.fx = null;
    d.fy = null;
}