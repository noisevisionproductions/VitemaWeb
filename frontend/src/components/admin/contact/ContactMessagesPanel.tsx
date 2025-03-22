import React, {useState} from 'react';
import ContactMessagesList from './ContactMessagesList';
import ContactMessageDetail from './ContactMessageDetail';

const ContactMessagesPanel: React.FC = () => {
    const [selectedMessageId, setSelectedMessageId] = useState<string | null>(null);

    if (selectedMessageId) {
        return (
            <ContactMessageDetail
                id={selectedMessageId}
                onBack={() => setSelectedMessageId(null)}
            />
        );
    }

    return <ContactMessagesList onSelectMessage={setSelectedMessageId}/>;
};

export default ContactMessagesPanel;