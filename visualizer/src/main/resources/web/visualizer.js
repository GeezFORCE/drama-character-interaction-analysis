// Drama Character Interaction Visualizer

// Global variables
let svg;
let simulation;
let dramaSelect = document.getElementById('drama-select');
let characterInfo = document.getElementById('character-info');
let tooltip;
let zoom;
let g; // Main group for zooming/panning
let width, height;

// Initialize the visualization
document.addEventListener('DOMContentLoaded', function() {
    // Get container dimensions
    const container = document.getElementById('graph');
    width = container.clientWidth || 800;
    height = container.clientHeight || 600;

    // Create SVG container
    svg = d3.select('#graph')
        .append('svg')
        .attr('width', '100%')
        .attr('height', '100%')
        .attr('viewBox', `0 0 ${width} ${height}`);

    // Create main group for zoom/pan
    g = svg.append('g');

    // Create zoom behavior
    zoom = d3.zoom()
        .scaleExtent([0.1, 4])
        .on('zoom', function(event) {
            g.attr('transform', event.transform);
        });

    // Apply zoom to SVG
    svg.call(zoom);

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

    // Handle window resize
    window.addEventListener('resize', function() {
        const container = document.getElementById('graph');
        width = container.clientWidth || 800;
        height = container.clientHeight || 600;
        svg.attr('viewBox', `0 0 ${width} ${height}`);

        // Restart simulation with new center if it exists
        if (simulation) {
            simulation.force('center', d3.forceCenter(width / 2, height / 2));
            simulation.alpha(0.3).restart();
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

    // Calculate node radius based on number of nodes (responsive sizing)
    const nodeRadius = Math.max(8, Math.min(15, 200 / Math.sqrt(data.nodes.length)));

    // Calculate forces based on graph size
    const linkDistance = Math.max(50, Math.min(150, 300 / Math.sqrt(data.nodes.length)));
    const chargeStrength = Math.max(-500, -100 * Math.sqrt(data.nodes.length));

    // Create force simulation with boundary constraints
    simulation = d3.forceSimulation(data.nodes)
        .force('link', d3.forceLink(data.links).id(d => d.id).distance(linkDistance))
        .force('charge', d3.forceManyBody().strength(chargeStrength))
        .force('center', d3.forceCenter(width / 2, height / 2))
        .force('collision', d3.forceCollide().radius(nodeRadius + 5))
        .force('x', d3.forceX(width / 2).strength(0.1))
        .force('y', d3.forceY(height / 2).strength(0.1));

    // Create links
    const link = g.append('g')
        .attr('class', 'links')
        .selectAll('line')
        .data(data.links)
        .enter()
        .append('line')
        .attr('class', 'link')
        .style('stroke', '#999')
        .style('stroke-opacity', 0.6)
        .style('stroke-width', d => Math.sqrt(d.value));

    // Create nodes
    const node = g.append('g')
        .attr('class', 'nodes')
        .selectAll('circle')
        .data(data.nodes)
        .enter()
        .append('circle')
        .attr('class', d => `node ${d.gender.toLowerCase()}`)
        .attr('r', nodeRadius)
        .style('fill', d => d.gender.toLowerCase() === 'male' ? '#4285f4' : '#ea4335')
        .style('stroke', '#fff')
        .style('stroke-width', 2)
        .call(d3.drag()
            .on('start', dragstarted)
            .on('drag', dragged)
            .on('end', dragended));

    // Add node labels
    const labels = g.append('g')
        .attr('class', 'labels')
        .selectAll('text')
        .data(data.nodes)
        .enter()
        .append('text')
        .text(d => d.name)
        .attr('x', nodeRadius + 2)
        .attr('y', 3)
        .style('font-size', '12px')
        .style('font-family', 'Arial, sans-serif')
        .style('fill', '#333')
        .style('pointer-events', 'none');

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

            // Center the clicked node
            const transform = d3.zoomTransform(svg.node());
            const x = transform.invertX(width / 2) - d.x;
            const y = transform.invertY(height / 2) - d.y;

            svg.transition()
                .duration(750)
                .call(zoom.transform, d3.zoomIdentity.translate(x, y).scale(transform.k));
        });

    // Update positions on simulation tick with boundary constraints
    simulation.on('tick', () => {
        // Constrain nodes to stay within expanded boundaries
        data.nodes.forEach(d => {
            const margin = nodeRadius + 10;
            d.x = Math.max(margin, Math.min(width - margin, d.x));
            d.y = Math.max(margin, Math.min(height - margin, d.y));
        });

        link
            .attr('x1', d => d.source.x)
            .attr('y1', d => d.source.y)
            .attr('x2', d => d.target.x)
            .attr('y2', d => d.target.y);

        node
            .attr('cx', d => d.x)
            .attr('cy', d => d.y);

        labels
            .attr('x', d => d.x + nodeRadius + 2)
            .attr('y', d => d.y + 3);
    });

    // Add zoom controls
    addZoomControls();

    // Reset zoom to fit all nodes
    setTimeout(() => {
        fitToView();
    }, 1000);
}

// Add zoom control buttons
function addZoomControls() {
    // Remove existing controls
    d3.select('#zoom-controls').remove();

    const controls = d3.select('#graph')
        .append('div')
        .attr('id', 'zoom-controls')
        .style('position', 'absolute')
        .style('top', '10px')
        .style('right', '10px')
        .style('z-index', '1000');

    // Zoom in button
    controls.append('button')
        .text('+')
        .style('margin', '2px')
        .style('padding', '5px 10px')
        .style('cursor', 'pointer')
        .on('click', function() {
            svg.transition().duration(300).call(zoom.scaleBy, 1.5);
        });

    // Zoom out button
    controls.append('button')
        .text('-')
        .style('margin', '2px')
        .style('padding', '5px 10px')
        .style('cursor', 'pointer')
        .on('click', function() {
            svg.transition().duration(300).call(zoom.scaleBy, 1 / 1.5);
        });

    // Reset zoom button
    controls.append('button')
        .text('Reset')
        .style('margin', '2px')
        .style('padding', '5px 10px')
        .style('cursor', 'pointer')
        .on('click', function() {
            fitToView();
        });
}

// Fit the graph to view
function fitToView() {
    if (!simulation || !simulation.nodes().length) return;

    const nodes = simulation.nodes();
    const margin = 50;

    // Calculate bounds
    const xExtent = d3.extent(nodes, d => d.x);
    const yExtent = d3.extent(nodes, d => d.y);

    const graphWidth = xExtent[1] - xExtent[0];
    const graphHeight = yExtent[1] - yExtent[0];

    // Calculate scale to fit
    const scale = Math.min(
        (width - margin * 2) / graphWidth,
        (height - margin * 2) / graphHeight,
        2 // Maximum scale
    );

    // Calculate translation to center
    const centerX = (xExtent[0] + xExtent[1]) / 2;
    const centerY = (yExtent[0] + yExtent[1]) / 2;

    const translateX = width / 2 - centerX * scale;
    const translateY = height / 2 - centerY * scale;

    // Apply transform
    svg.transition()
        .duration(750)
        .call(zoom.transform, d3.zoomIdentity.translate(translateX, translateY).scale(scale));
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
    g.selectAll('*').remove();
    d3.select('#zoom-controls').remove();
    characterInfo.innerHTML = '<p>Select a character in the graph to see details.</p>';

    // Reset zoom
    svg.call(zoom.transform, d3.zoomIdentity);
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