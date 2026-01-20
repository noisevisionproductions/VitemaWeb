import {useState} from "react";

interface ConfirmationState<T = any> {
    isOpen: boolean;
    data?: T;
}

export function useConfirmation<T = any>() {
    const [state, setState] = useState<ConfirmationState<T>>({
        isOpen: false,
        data: undefined
    });

    const openConfirmation = (data?: T) => {
        setState({isOpen: true, data});
    };

    const closeConfirmation = () => {
        setState({isOpen: false, data: undefined});
    };

    return {
        isConfirmationOpen: state.isOpen,
        confirmationData: state.data,
        openConfirmation,
        closeConfirmation,
    };
}