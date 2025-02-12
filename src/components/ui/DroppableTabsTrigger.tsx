import React from 'react';
import {TabsTrigger} from './Tabs';
import {useDrop} from 'react-dnd';
import {ParsedProduct} from '../../types/product';

type BaseTabsTriggerProps = Omit<React.ComponentPropsWithoutRef<typeof TabsTrigger>, 'onDrop'>;

interface DroppableTabsTriggerProps extends BaseTabsTriggerProps {
    onDrop?: (product: ParsedProduct) => void;
    categoryColor?: string;
    isActive?: boolean;
}

type DropItem = {
    product: ParsedProduct;
    fromCategory: boolean;
};

export const DroppableTabsTrigger = React.forwardRef<
    React.ElementRef<typeof TabsTrigger>,
    DroppableTabsTriggerProps
>(({onDrop, categoryColor, children, className, ...props}) => {
    const [{isOver}, drop] = useDrop<DropItem, void, { isOver: boolean }>(() => ({
        accept: 'PRODUCT',
        drop: (item) => {
            if (!item.fromCategory && onDrop) {
                onDrop(item.product);
            }
        },
        collect: (monitor) => ({
            isOver: monitor.isOver()
        })
    }));

    return (
        <div ref={drop}>
            <TabsTrigger
                {...props}
                className={`
                    ${className}
                    ${isOver ? 'ring-2 ring-blue-400' : ''}
                    transition-all duration-200
                `}
                style={{
                    backgroundColor: categoryColor ? categoryColor + '15' : undefined,
                    ...(isOver && {transform: 'scale(1.02)'})
                }}
            >
                {children}
            </TabsTrigger>
        </div>
    );
});

DroppableTabsTrigger.displayName = 'DroppableTabsTrigger';