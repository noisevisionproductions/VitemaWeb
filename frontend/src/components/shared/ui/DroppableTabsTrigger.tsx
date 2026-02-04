import React from 'react';
import {TabsTrigger} from './Tabs';
import {useDrop} from 'react-dnd';
import {Product} from '../../../types/product';

type BaseTabsTriggerProps = Omit<React.ComponentPropsWithoutRef<typeof TabsTrigger>, 'onDrop'>;

interface DroppableTabsTriggerProps extends BaseTabsTriggerProps {
    onDrop?: (product: Product) => void;
    categoryColor?: string;
    isActive?: boolean;
}

type DropItem = {
    product: Product;
    fromCategory: boolean;
};

export const DroppableTabsTrigger = React.forwardRef<
    React.ElementRef<typeof TabsTrigger>,
    DroppableTabsTriggerProps
>(({onDrop, categoryColor, children, className, ...props}, ref) => {
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
                ref={ref}
                {...props}
                className={`
                    ${className}
                    ${isOver ? 'ring-2 ring-blue-400 dark:ring-blue-500' : ''}
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