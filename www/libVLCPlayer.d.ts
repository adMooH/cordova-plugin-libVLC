interface IlibVLCPlayer {
	play(
		url: string,
		options?: IlibVLCPlayerOptions,
		success?: (event: IlibVLCPlayerEvent) => void,
		failed?: () => void,
	): void;
	playNext(
		url: string,
		options: IlibVLCPlayerBaseOptions,
		success?: () => void,
		failed?: () => void,
	): void;
	stop(success?: () => void, failed?: () => void): void;
	pause(success?: () => void, failed?: () => void): void;
	close(success?: () => void, failed?: () => void): void;

	// returns: {position, current_location (00:00), duration (00:00)}
	getPosition(
		success?: (position: number, current_location: string, duration: string) => void,
		failed?: () => void,
	): void;

	// position must be 1 to 100 only
	seekPosition(position: number, success?: () => void, failed?: () => void): void;

	changePositionAndSize(
		options: IlibVLCPlayerPositionAndSize,
		success?: () => void,
		failed?: () => void,
	);
}

interface IlibVLCPlayerEvent {
	event_name: string;
	data: any;
}

interface IlibVLCPlayerBaseOptions {
	autoPlay?: bool;
	hideControls?: bool;
}

interface IlibVLCPlayerOptions extends IlibVLCPlayerBaseOptions, IlibVLCPlayerPositionAndSize {}

interface IlibVLCPlayerPositionAndSize {
	fullscreen?: bool;
	top?: number;
	left?: number;
	width?: number;
	height?: number;
}

interface Window {
	libVLCPlayer: IlibVLCPlayer;
}
