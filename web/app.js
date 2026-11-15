const { createApp } = Vue;

createApp({
    data() {
        return {
            state: { total: 0, available: 0, spaces: [], vehicles: [], history: [] },
            level: 1,
            plate: '',
            type: 'Car',
            space: '1-C-1',
            highlight: '',
            busy: false,
            connected: false,
            failed: false,
            message: 'Connecting to the parking server...'
        };
    },
    methods: {
        rowSpaces(row) {
            return this.state.spaces.filter(
                item => item.level === this.level && item.row === row);
        },
        shortType(type) {
            return { MOTORCYCLE: 'M', COMPACT: 'C', LARGE: 'L' }[type];
        },
        spaceLabel(item) {
            return `${item.id}, ${item.type}, ${
                item.available ? 'Available' : 'Occupied by ' + item.plate
            }`;
        },
        selectSpace(item) {
            this.space = item.id;
            if (item.plate) {
                this.plate = item.plate;
                this.type = item.vehicleType;
            }
        },
        locations(plate) {
            return this.state.spaces
                .filter(item => item.plate === plate)
                .map(item => item.id).join(', ');
        },
        async refresh() {
            if (this.busy) return;
            this.busy = true;
            try {
                const response = await fetch('/api/state');
                if (!response.ok) throw new Error('Could not load parking data.');
                this.state = await response.json();
                this.connected = true;
                this.failed = false;
                this.message = 'Connected. Select a space and enter a plate.';
            } catch (error) {
                this.connected = false;
                this.failed = true;
                this.message = 'Connection failed. Check the Java server and refresh.';
            } finally {
                this.busy = false;
            }
        },
        async act(action, overrides = {}) {
            if (this.busy || !this.connected) return;
            if (!this.plate.trim() && !overrides.plate) {
                this.failed = true;
                this.message = 'Enter a plate number.';
                return;
            }

            this.busy = true;
            const request = {
                action,
                plate: this.plate.trim(),
                type: this.type,
                space: this.space,
                ...overrides
            };

            try {
                const response = await fetch('/api/action', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(request)
                });
                const result = await response.json();
                if (!response.ok) {
                    throw new Error(result.message || 'Action blocked.');
                }

                this.state = result;
                this.failed = false;
                this.message = result.message;
                this.highlight = '';

                if (action === 'search') {
                    this.highlight = request.plate.trim().toUpperCase();
                    const found = result.spaces.find(
                        item => item.plate === this.highlight);
                    if (found) this.level = found.level;
                }
            } catch (error) {
                this.failed = true;
                this.message = error.message;
            } finally {
                this.busy = false;
            }
        },
        drag(event, item) {
            if (this.busy || !item.plate) {
                event.preventDefault();
                return;
            }
            event.dataTransfer.setData('text/plain', item.plate);
            event.dataTransfer.effectAllowed = 'move';
        },
        async drop(event, item) {
            const plate = event.dataTransfer.getData('text/plain');
            if (!plate) return;
            this.plate = plate;
            this.space = item.id;
            await this.act('move', { plate, space: item.id });
        }
    },
    mounted() {
        this.refresh();
    }
}).mount('#app');
